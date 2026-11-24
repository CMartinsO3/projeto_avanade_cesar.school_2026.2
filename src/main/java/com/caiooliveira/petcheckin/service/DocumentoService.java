package com.caiooliveira.petcheckin.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.caiooliveira.petcheckin.dto.DocumentoResponse;
import com.caiooliveira.petcheckin.dto.ResultadoExtracao;
import com.caiooliveira.petcheckin.dto.ResultadoOcr;
import com.caiooliveira.petcheckin.exception.ProcessamentoDocumentoException;
import com.caiooliveira.petcheckin.exception.RecursoNaoEncontradoException;
import com.caiooliveira.petcheckin.model.Documento;
import com.caiooliveira.petcheckin.model.Reserva;
import com.caiooliveira.petcheckin.model.StatusDocumento;
import com.caiooliveira.petcheckin.model.TipoDocumento;
import com.caiooliveira.petcheckin.repository.DocumentoRepository;
import com.caiooliveira.petcheckin.repository.ReservaRepository;

import tools.jackson.databind.json.JsonMapper;

@Service
public class DocumentoService {

    private static final Logger log = LoggerFactory.getLogger(DocumentoService.class);

    private final DocumentoRepository documentoRepository;
    private final ReservaRepository reservaRepository;
    private final ArmazenamentoService armazenamentoService;
    private final OcrService ocrService;
    private final ExtracaoService extracaoService;
    private final JsonMapper jsonMapper;

    public DocumentoService(DocumentoRepository documentoRepository,
                            ReservaRepository reservaRepository,
                            ArmazenamentoService armazenamentoService,
                            OcrService ocrService,
                            ExtracaoService extracaoService,
                            JsonMapper jsonMapper) {
        this.documentoRepository = documentoRepository;
        this.reservaRepository = reservaRepository;
        this.armazenamentoService = armazenamentoService;
        this.ocrService = ocrService;
        this.extracaoService = extracaoService;
        this.jsonMapper = jsonMapper;
    }

    public DocumentoResponse enviar(Long reservaId, TipoDocumento tipo, MultipartFile arquivo) {
        Reserva reserva = buscarReserva(reservaId);

        Path caminho = armazenamentoService.salvar(arquivo);

        Documento documento = documentoRepository.save(new Documento(
                reserva,
                tipo,
                Objects.requireNonNullElse(arquivo.getOriginalFilename(), "arquivo"),
                caminho.toString(),
                arquivo.getContentType(),
                arquivo.getSize()
        ));

        processarOcr(documento);
        if (documento.getStatus() == StatusDocumento.PROCESSADO
                && documento.getTipo() == TipoDocumento.CARTEIRA_VACINACAO) {
            extrairDados(documento);
        }
        return DocumentoResponse.de(documentoRepository.save(documento));
    }

    public List<DocumentoResponse> listarPorReserva(Long reservaId) {
        buscarReserva(reservaId);
        return documentoRepository.findByReservaId(reservaId)
                .stream()
                .map(DocumentoResponse::de)
                .toList();
    }

    private void processarOcr(Documento documento) {
        try {
            ResultadoOcr resultado = ocrService.extrairTexto(
                    Path.of(documento.getCaminhoArquivo()), documento.getContentType());
            documento.registrarOcr(resultado.texto(), resultado.confianca(), resultado.quantidadePalavras());
            log.info("OCR do documento {} concluído: {} palavras, confiança {}%",
                    documento.getId(), resultado.quantidadePalavras(), resultado.confianca());
        } catch (ProcessamentoDocumentoException e) {
            documento.registrarFalhaProcessamento();
            log.warn("Falha no OCR do documento {}: {}", documento.getId(), e.getMessage());
        }
    }

    private void extrairDados(Documento documento) {
        ResultadoExtracao resultado = extracaoService.extrair(documento.getTextoExtraido());
        String json = jsonMapper.writeValueAsString(resultado.dados());
        documento.registrarExtracao(json, resultado.metodo());
        log.info("Dados do documento {} extraídos com {}", documento.getId(), resultado.metodo());
    }

    private Reserva buscarReserva(Long reservaId) {
        return reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva não encontrada com id " + reservaId));
    }
}