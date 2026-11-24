package com.caiooliveira.petcheckin.dto;

import com.caiooliveira.petcheckin.model.MetodoExtracao;

public record ResultadoExtracao(
        DadosCarteira dados,
        MetodoExtracao metodo
) {
}
