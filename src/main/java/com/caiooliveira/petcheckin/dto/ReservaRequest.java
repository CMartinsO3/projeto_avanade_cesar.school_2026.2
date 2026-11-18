package com.caiooliveira.petcheckin.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

public record ReservaRequest(

        @NotNull(message = "O id do pet é obrigatório")
        Long petId,

        @NotNull(message = "A data de check-in é obrigatória")
        @FutureOrPresent(message = "A data de check-in não pode estar no passado")
        LocalDate dataCheckin,

        @NotNull(message = "A data de check-out é obrigatória")
        LocalDate dataCheckout
) {
}