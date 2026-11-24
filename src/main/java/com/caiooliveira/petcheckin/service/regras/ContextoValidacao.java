package com.caiooliveira.petcheckin.service.regras;

import com.caiooliveira.petcheckin.dto.DadosCarteira;
import com.caiooliveira.petcheckin.model.Documento;
import com.caiooliveira.petcheckin.model.Pet;
import com.caiooliveira.petcheckin.model.Reserva;

/**
 * Tudo o que as regras precisam saber: a reserva (com o pet cadastrado),
 * o documento (resultado do OCR) e os dados extraídos da carteira.
 */
public record ContextoValidacao(
        Reserva reserva,
        Documento documento,
        DadosCarteira dados
) {

    public Pet pet() {
        return reserva.getPet();
    }
}
