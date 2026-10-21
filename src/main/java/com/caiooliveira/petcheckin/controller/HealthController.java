package com.caiooliveira.petcheckin.controller;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.caiooliveira.petcheckin.dto.HealthResponse;

@RestController
public class HealthController {

    @GetMapping("/health")
    public HealthResponse verificarSaude() {
        return new HealthResponse(
                "UP",
                "PetCheck-in API está funcionando 🐾",
                LocalDateTime.now());
    }
}