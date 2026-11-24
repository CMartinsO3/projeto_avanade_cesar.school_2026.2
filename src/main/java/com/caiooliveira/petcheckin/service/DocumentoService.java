package com.caiooliveira.petcheckin.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.caiooliveira.petcheckin.dto.DocumentoResponse;
import com.caiooliveira.petcheckin.dto.ResultadoOcr;
import com.caiooliveira.petcheckin.exception.ProcessamentoDocumentoException;
import com.caiooliveira.petcheckin.exception.RecursoNaoEncontradoException;
import com.caiooliveira.petcheckin.model.Documento;
import com.caiooliveira.petcheckin.model.Reserva;
import com.caiooliveira.petcheckin.model.TipoDocumento;
import com.caiooliveira.petcheckin.repository.DocumentoRepository;
import com.caiooliveira.petcheckin.repository.ReservaRepository;

@Service
public class DocumentoService {

    private static final Logger log = LoggerFactory.getLogger(DocumentoService.class);

    private final DocumentoRepository documentoRepository;
    private final ReservaRepository reservaRepository;
    private final ArmazenamentoService armazenamentoService;
    private final OcrService ocrService;

    public DocumentoService(DocumentoRepository documentoRepository,
                            ReservaRepository reservaRepository,
                            ArmazenamentoService armazenamentoService,
                            OcrService ocrService) {
        this.documentoRepository = documentoRepository;
        this.reservaRepository = reservaRepository;
        this.armazenamentoService = armazenamentoService;
        this.ocrService = ocrService;
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

    private Reserva buscarReserva(Long reservaId) {
        return reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva não encontrada com id " + reservaId));
    }
}