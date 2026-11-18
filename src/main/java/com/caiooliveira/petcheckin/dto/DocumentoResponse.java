package com.caiooliveira.petcheckin.dto;

import java.time.LocalDateTime;

import com.caiooliveira.petcheckin.model.Documento;
import com.caiooliveira.petcheckin.model.StatusDocumento;
import com.caiooliveira.petcheckin.model.TipoDocumento;

public record DocumentoResponse(
        Long id,
        Long reservaId,
        TipoDocumento tipo,
        String nomeOriginal,
        String contentType,
        Long tamanhoBytes,
        LocalDateTime dataEnvio,
        StatusDocumento status
) {

    public static DocumentoResponse de(Documento documento) {
        return new DocumentoResponse(
                documento.getId(),
                documento.getReserva().getId(),
                documento.getTipo(),
                documento.getNomeOriginal(),
                documento.getContentType(),
                documento.getTamanhoBytes(),
                documento.getDataEnvio(),
                documento.getStatus()
        );
    }
}