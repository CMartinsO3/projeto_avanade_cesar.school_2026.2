package com.caiooliveira.petcheckin.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.caiooliveira.petcheckin.dto.DocumentoResponse;
import com.caiooliveira.petcheckin.model.TipoDocumento;
import com.caiooliveira.petcheckin.service.DocumentoService;

@RestController
@RequestMapping("/api/reservas/{reservaId}/documentos")
public class DocumentoController {

    private final DocumentoService documentoService;

    public DocumentoController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentoResponse enviar(@PathVariable Long reservaId,
                                    @RequestParam TipoDocumento tipo,
                                    @RequestParam MultipartFile arquivo) {
        return documentoService.enviar(reservaId, tipo, arquivo);
    }

    @GetMapping
    public List<DocumentoResponse> listar(@PathVariable Long reservaId) {
        return documentoService.listarPorReserva(reservaId);
    }
}