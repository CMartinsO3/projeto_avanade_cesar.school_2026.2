package com.caiooliveira.petcheckin.dto;

public record EtapaValidacao(
        String nome,
        boolean concluida,
        String detalhe
) {
}
