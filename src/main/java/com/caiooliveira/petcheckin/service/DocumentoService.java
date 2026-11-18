package com.caiooliveira.petcheckin.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.caiooliveira.petcheckin.dto.DocumentoResponse;
import com.caiooliveira.petcheckin.exception.RecursoNaoEncontradoException;
import com.caiooliveira.petcheckin.model.Documento;
import com.caiooliveira.petcheckin.model.Reserva;
import com.caiooliveira.petcheckin.model.TipoDocumento;
import com.caiooliveira.petcheckin.repository.DocumentoRepository;
import com.caiooliveira.petcheckin.repository.ReservaRepository;

@Service
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final ReservaRepository reservaRepository;
    private final ArmazenamentoService armazenamentoService;

    public DocumentoService(DocumentoRepository documentoRepository,
                            ReservaRepository reservaRepository,
                            ArmazenamentoService armazenamentoService) {
        this.documentoRepository = documentoRepository;
        this.reservaRepository = reservaRepository;
        this.armazenamentoService = armazenamentoService;
    }

    public DocumentoResponse enviar(Long reservaId, TipoDocumento tipo, MultipartFile arquivo) {
        Reserva reserva = buscarReserva(reservaId);

        Path caminho = armazenamentoService.salvar(arquivo);

        Documento documento = new Documento(
                reserva,
                tipo,
                Objects.requireNonNullElse(arquivo.getOriginalFilename(), "arquivo"),
                caminho.toString(),
                arquivo.getContentType(),
                arquivo.getSize()
        );
        return DocumentoResponse.de(documentoRepository.save(documento));
    }

    public List<DocumentoResponse> listarPorReserva(Long reservaId) {
        buscarReserva(reservaId);
        return documentoRepository.findByReservaId(reservaId)
                .stream()
                .map(DocumentoResponse::de)
                .toList();
    }

    private Reserva buscarReserva(Long reservaId) {
        return reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva não encontrada com id " + reservaId));
    }
}