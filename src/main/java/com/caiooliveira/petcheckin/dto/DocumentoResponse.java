package com.caiooliveira.petcheckin.dto;

import java.time.LocalDateTime;

import com.caiooliveira.petcheckin.model.Documento;
import com.caiooliveira.petcheckin.model.MetodoExtracao;
import com.caiooliveira.petcheckin.model.StatusDocumento;
import com.caiooliveira.petcheckin.model.TipoDocumento;
import com.fasterxml.jackson.annotation.JsonRawValue;

public record DocumentoResponse(
        Long id,
        Long reservaId,
        TipoDocumento tipo,
        String nomeOriginal,
        String contentType,
        Long tamanhoBytes,
        LocalDateTime dataEnvio,
        StatusDocumento status,
        Double confiancaOcr,
        Integer palavrasOcr,
        String textoExtraido,
        MetodoExtracao metodoExtracao,
        @JsonRawValue String dadosExtraidos
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
                documento.getStatus(),
                documento.getConfiancaOcr(),
                documento.getPalavrasOcr(),
                documento.getTextoExtraido(),
                documento.getMetodoExtracao(),
                documento.getDadosExtraidos()
        );
    }
}
