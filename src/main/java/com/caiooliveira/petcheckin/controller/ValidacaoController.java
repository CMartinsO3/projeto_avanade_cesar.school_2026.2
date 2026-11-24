package com.caiooliveira.petcheckin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.caiooliveira.petcheckin.dto.ValidacaoResponse;
import com.caiooliveira.petcheckin.service.ValidacaoService;

@RestController
@RequestMapping("/api/reservas/{reservaId}/validacao")
public class ValidacaoController {

    private final ValidacaoService validacaoService;

    public ValidacaoController(ValidacaoService validacaoService) {
        this.validacaoService = validacaoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ValidacaoResponse validar(@PathVariable Long reservaId) {
        return validacaoService.validar(reservaId);
    }

    @GetMapping
    public ValidacaoResponse buscarUltima(@PathVariable Long reservaId) {
        return validacaoService.buscarUltima(reservaId);
    }
}
