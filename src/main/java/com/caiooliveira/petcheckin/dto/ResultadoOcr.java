package com.caiooliveira.petcheckin.dto;

public record ResultadoOcr(
        String texto,
        double confianca,
        int quantidadePalavras
) {
}