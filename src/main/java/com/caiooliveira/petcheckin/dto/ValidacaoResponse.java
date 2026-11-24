package com.caiooliveira.petcheckin.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.caiooliveira.petcheckin.model.Documento;
import com.caiooliveira.petcheckin.model.StatusReserva;
import com.caiooliveira.petcheckin.model.StatusValidacao;
import com.caiooliveira.petcheckin.model.Validacao;

public record ValidacaoResponse(
        boolean aprovado,
        StatusValidacao status,
        List<String> regrasAtendidas,
        List<String> pendencias,
        List<EtapaValidacao> etapas,
        Long id,
        Long reservaId,
        StatusReserva statusReserva,
        Long documentoId,
        LocalDateTime dataHora
) {

    public static ValidacaoResponse de(Validacao validacao) {
        return new ValidacaoResponse(
                validacao.getStatus() == StatusValidacao.APTO,
                validacao.getStatus(),
                validacao.getRegrasAtendidas(),
                validacao.getPendencias(),
                montarEtapas(validacao),
                validacao.getId(),
                validacao.getReserva().getId(),
                validacao.getReserva().getStatus(),
                validacao.getDocumento().getId(),
                validacao.getDataHora()
        );
    }

    // As 4 etapas do stepper do front: Upload → OCR → Extração → Validação
    private static List<EtapaValidacao> montarEtapas(Validacao validacao) {
        Documento documento = validacao.getDocumento();
        return List.of(
                new EtapaValidacao("Upload", true, documento.getNomeOriginal()),
                new EtapaValidacao("OCR", true, "Confiança de " + documento.getConfiancaOcr() + "%"),
                new EtapaValidacao("Extração", true, "Dados extraídos com " + documento.getMetodoExtracao()),
                new EtapaValidacao("Validação", true, validacao.getStatus().name())
        );
    }
}
