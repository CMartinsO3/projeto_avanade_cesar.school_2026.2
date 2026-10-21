package com.caiooliveira.petcheckin.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.caiooliveira.petcheckin.dto.PetRequest;
import com.caiooliveira.petcheckin.dto.PetResponse;
import com.caiooliveira.petcheckin.model.Pet;
import com.caiooliveira.petcheckin.repository.PetRepository;

@Service
public class PetService {

    private final PetRepository petRepository;

    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    public PetResponse cadastrar(PetRequest request) {
        Pet pet = new Pet(
                request.nome(),
                request.nomeTutor(),
                request.especie(),
                request.raca(),
                request.dataNascimento()
        );
        Pet petSalvo = petRepository.save(pet);
        return PetResponse.de(petSalvo);
    }

    public List<PetResponse> listar() {
        return petRepository.findAll()
                .stream()
                .map(PetResponse::de)
                .toList();
    }

    public PetResponse buscarPorId(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pet não encontrado com id " + id));
        return PetResponse.de(pet);
    }
}