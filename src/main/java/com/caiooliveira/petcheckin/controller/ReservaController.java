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

import com.caiooliveira.petcheckin.dto.ReservaRequest;
import com.caiooliveira.petcheckin.dto.ReservaResponse;
import com.caiooliveira.petcheckin.service.ReservaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse cadastrar(@RequestBody @Valid ReservaRequest request) {
        return reservaService.cadastrar(request);
    }

    @GetMapping
    public List<ReservaResponse> listar() {
        return reservaService.listar();
    }

    @GetMapping("/{id}")
    public ReservaResponse buscarPorId(@PathVariable Long id) {
        return reservaService.buscarPorId(id);
    }
}