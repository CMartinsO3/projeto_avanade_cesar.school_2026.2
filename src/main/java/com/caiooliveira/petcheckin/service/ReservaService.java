package com.caiooliveira.petcheckin.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.caiooliveira.petcheckin.dto.ReservaRequest;
import com.caiooliveira.petcheckin.dto.ReservaResponse;
import com.caiooliveira.petcheckin.exception.RecursoNaoEncontradoException;
import com.caiooliveira.petcheckin.exception.RegraNegocioException;
import com.caiooliveira.petcheckin.model.Pet;
import com.caiooliveira.petcheckin.model.Reserva;
import com.caiooliveira.petcheckin.repository.PetRepository;
import com.caiooliveira.petcheckin.repository.ReservaRepository;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final PetRepository petRepository;

    public ReservaService(ReservaRepository reservaRepository, PetRepository petRepository) {
        this.reservaRepository = reservaRepository;
        this.petRepository = petRepository;
    }

    public ReservaResponse cadastrar(ReservaRequest request) {
        Pet pet = petRepository.findById(request.petId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Pet não encontrado com id " + request.petId()));

        if (!request.dataCheckout().isAfter(request.dataCheckin())) {
            throw new RegraNegocioException(
                    "A data de check-out deve ser posterior à data de check-in");
        }

        Reserva reserva = new Reserva(pet, request.dataCheckin(), request.dataCheckout());
        Reserva reservaSalva = reservaRepository.save(reserva);
        return ReservaResponse.de(reservaSalva);
    }

    public List<ReservaResponse> listar() {
        return reservaRepository.findAll()
                .stream()
                .map(ReservaResponse::de)
                .toList();
    }

    public ReservaResponse buscarPorId(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva não encontrada com id " + id));
        return ReservaResponse.de(reserva);
    }
}