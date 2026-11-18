package com.caiooliveira.petcheckin.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroApiResponse(
        int status,
        String erro,
        Map<String, String> campos
) {
}