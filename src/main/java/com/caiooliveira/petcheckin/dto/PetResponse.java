package com.caiooliveira.petcheckin.dto;

import java.time.LocalDate;

import com.caiooliveira.petcheckin.model.Especie;
import com.caiooliveira.petcheckin.model.Pet;

public record PetResponse(
        Long id,
        String nome,
        String nomeTutor,
        Especie especie,
        String raca,
        LocalDate dataNascimento
) {

    public static PetResponse de(Pet pet) {
        return new PetResponse(
                pet.getId(),
                pet.getNome(),
                pet.getNomeTutor(),
                pet.getEspecie(),
                pet.getRaca(),
                pet.getDataNascimento()
        );
    }
}