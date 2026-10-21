package com.caiooliveira.petcheckin.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.caiooliveira.petcheckin.dto.PetRequest;
import com.caiooliveira.petcheckin.dto.PetResponse;
import com.caiooliveira.petcheckin.service.PetService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PetResponse cadastrar(@RequestBody @Valid PetRequest request) {
        return petService.cadastrar(request);
    }

    @GetMapping
    public List<PetResponse> listar() {
        return petService.listar();
    }

    @GetMapping("/{id}")
    public PetResponse buscarPorId(@PathVariable Long id) {
        return petService.buscarPorId(id);
    }
}