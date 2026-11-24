package com.caiooliveira.petcheckin.dto;

import java.time.LocalDate;

import com.caiooliveira.petcheckin.model.TipoVacina;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Vacina(
        String nome,
        TipoVacina tipo,
        LocalDate dataAplicacao,
        LocalDate proximaDose
) {
}
