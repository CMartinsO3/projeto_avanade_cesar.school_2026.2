package com.caiooliveira.petcheckin.dto;

import java.time.LocalDateTime;

public record HealthResponse(
        String status,
        String mensagem,
        LocalDateTime dataHora) {
}