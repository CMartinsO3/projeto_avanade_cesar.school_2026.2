package com.caiooliveira.petcheckin.dto;

import java.time.LocalDate;

import com.caiooliveira.petcheckin.model.Reserva;
import com.caiooliveira.petcheckin.model.StatusReserva;

public record ReservaResponse(
        Long id,
        PetResponse pet,
        LocalDate dataCheckin,
        LocalDate dataCheckout,
        StatusReserva status
) {

    public static ReservaResponse de(Reserva reserva) {
        return new ReservaResponse(
                reserva.getId(),
                PetResponse.de(reserva.getPet()),
                reserva.getDataCheckin(),
                reserva.getDataCheckout(),
                reserva.getStatus()
        );
    }
}