package com.caiooliveira.petcheckin.dto;

import java.time.LocalDate;

import com.caiooliveira.petcheckin.model.Especie;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

public record PetRequest(

        @NotBlank(message = "O nome do pet é obrigatório")
        String nome,

        @NotBlank(message = "O nome do tutor é obrigatório")
        String nomeTutor,

        @NotNull(message = "A espécie é obrigatória (CACHORRO ou GATO)")
        Especie especie,

        String raca,

        @Past(message = "A data de nascimento precisa estar no passado")
        LocalDate dataNascimento
) {
}