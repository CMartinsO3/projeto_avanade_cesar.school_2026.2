package com.caiooliveira.petcheckin.dto;

import java.time.LocalDate;
import java.util.List;

import com.caiooliveira.petcheckin.model.Especie;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DadosCarteira(
        String nomePet,
        Especie especie,
        String raca,
        LocalDate dataNascimento,
        String nomeTutor,
        String crmvVeterinario,
        List<Vacina> vacinas
) {

    public DadosCarteira {
        vacinas = vacinas == null ? List.of() : List.copyOf(vacinas);
    }
}
