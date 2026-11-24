package com.caiooliveira.petcheckin.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.caiooliveira.petcheckin.dto.DadosCarteira;
import com.caiooliveira.petcheckin.dto.ValidacaoResponse;
import com.caiooliveira.petcheckin.exception.RecursoNaoEncontradoException;
import com.caiooliveira.petcheckin.exception.RegraNegocioException;
import com.caiooliveira.petcheckin.model.Documento;
import com.caiooliveira.petcheckin.model.Reserva;
import com.caiooliveira.petcheckin.model.StatusDocumento;
import com.caiooliveira.petcheckin.model.StatusReserva;
import com.caiooliveira.petcheckin.model.StatusValidacao;
import com.caiooliveira.petcheckin.model.TipoDocumento;
import com.caiooliveira.petcheckin.model.Validacao;
import com.caiooliveira.petcheckin.repository.DocumentoRepository;
import com.caiooliveira.petcheckin.repository.ReservaRepository;
import com.caiooliveira.petcheckin.repository.ValidacaoRepository;
import com.caiooliveira.petcheckin.service.regras.ContextoValidacao;
import com.caiooliveira.petcheckin.service.regras.RegraValidacao;

import tools.jackson.databind.json.JsonMapper;

@Service
public class ValidacaoService {

    private static final Logger log = LoggerFactory.getLogger(ValidacaoService.class);

    private final List<RegraValidacao> regras;
    private final ReservaRepository reservaRepository;
    private final DocumentoRepository documentoRepository;
    private final ValidacaoRepository validacaoRepository;
    private final JsonMapper jsonMapper;
    private final Clock clock;

    // O Spring entrega aqui TODAS as classes que implementam RegraValidacao
    public ValidacaoService(List<RegraValidacao> regras,
                            ReservaRepository reservaRepository,
                            DocumentoRepository documentoRepository,
                            ValidacaoRepository validacaoRepository,
                            JsonMapper jsonMapper,
                            Clock clock) {
        this.regras = regras.stream()
                .sorted(Comparator.comparing(RegraValidacao::codigo))
                .toList();
        this.reservaRepository = reservaRepository;
        this.documentoRepository = documentoRepository;
        this.validacaoRepository = validacaoRepository;
        this.jsonMapper = jsonMapper;
        this.clock = clock;
    }

    @Transactional
    public ValidacaoResponse validar(Long reservaId) {
        Reserva reserva = buscarReserva(reservaId);
        Documento carteira = documentoRepository
                .findFirstByReservaIdAndTipoAndStatusAndDadosExtraidosIsNotNullOrderByIdDesc(
                        reservaId, TipoDocumento.CARTEIRA_VACINACAO, StatusDocumento.PROCESSADO)
                .orElseThrow(() -> new RegraNegocioException(
                        "Nenhuma carteira de vacinação processada para esta reserva. "
                                + "Envie a carteira antes de validar."));

        DadosCarteira dados = jsonMapper.readValue(carteira.getDadosExtraidos(), DadosCarteira.class);
        ContextoValidacao contexto = new ContextoValidacao(reserva, carteira, dados);

        List<String> regrasAtendidas = new ArrayList<>();
        // LinkedHashSet: mantém a ordem e não repete a mesma pendência (ex.: R3 e R5 sobre a antirrábica)
        Set<String> pendencias = new LinkedHashSet<>();
        StatusValidacao status = StatusValidacao.APTO;

        for (RegraValidacao regra : regras) {
            List<String> pendenciasDaRegra = regra.avaliar(contexto);
            if (pendenciasDaRegra.isEmpty()) {
                regrasAtendidas.add(regra.codigo() + " - " + regra.descricao());
            } else {
                pendencias.addAll(pendenciasDaRegra);
                status = pior(status, regra.statusSeFalhar());
            }
        }

        reserva.setStatus(statusDaReserva(status));
        Validacao validacao = validacaoRepository.save(new Validacao(
                reserva, carteira, status, regrasAtendidas, List.copyOf(pendencias), LocalDateTime.now(clock)));

        log.info("Reserva {} validada com o documento {}: {}", reservaId, carteira.getId(), status);
        return ValidacaoResponse.de(validacao);
    }

    @Transactional(readOnly = true)
    public ValidacaoResponse buscarUltima(Long reservaId) {
        buscarReserva(reservaId);
        return validacaoRepository.findFirstByReservaIdOrderByIdDesc(reservaId)
                .map(ValidacaoResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nenhuma validação encontrada para a reserva " + reservaId));
    }

    // NAO_APTO "ganha" de EM_ANALISE_MANUAL, que "ganha" de APTO
    private StatusValidacao pior(StatusValidacao atual, StatusValidacao novo) {
        if (atual == StatusValidacao.NAO_APTO || novo == StatusValidacao.NAO_APTO) {
            return StatusValidacao.NAO_APTO;
        }
        if (atual == StatusValidacao.EM_ANALISE_MANUAL || novo == StatusValidacao.EM_ANALISE_MANUAL) {
            return StatusValidacao.EM_ANALISE_MANUAL;
        }
        return StatusValidacao.APTO;
    }

    private StatusReserva statusDaReserva(StatusValidacao status) {
        return switch (status) {
            case APTO -> StatusReserva.CONFIRMADA;
            case NAO_APTO -> StatusReserva.RECUSADA;
            case EM_ANALISE_MANUAL -> StatusReserva.EM_ANALISE_MANUAL;
        };
    }

    private Reserva buscarReserva(Long reservaId) {
        return reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva não encontrada com id " + reservaId));
    }
}
