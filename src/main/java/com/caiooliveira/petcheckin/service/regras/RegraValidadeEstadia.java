package com.caiooliveira.petcheckin.service.regras;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.caiooliveira.petcheckin.dto.Vacina;

/** R5 - antirrábica e polivalente continuam válidas até a data de check-out. */
@Component
public class RegraValidadeEstadia implements RegraValidacao {

    @Override
    public String codigo() {
        return "R5";
    }

    @Override
    public String descricao() {
        return "Vacinas válidas até o check-out";
    }

    @Override
    public List<String> avaliar(ContextoValidacao contexto) {
        LocalDate checkout = contexto.reserva().getDataCheckout();
        List<Vacina> vacinas = contexto.dados().vacinas();
        List<String> pendencias = new ArrayList<>();

        if (!validaAte(Vacinas.antirrabicaMaisRecente(vacinas), checkout)) {
            pendencias.add(Vacinas.MENSAGEM_VACINA.formatted(Vacinas.NOME_ANTIRRABICA));
        }
        Optional<Vacina> polivalente =
                Vacinas.polivalenteCompativelMaisRecente(vacinas, contexto.pet().getEspecie());
        if (!validaAte(polivalente, checkout)) {
            pendencias.add(Vacinas.MENSAGEM_VACINA.formatted(Vacinas.NOME_POLIVALENTE));
        }
        return pendencias;
    }

    private boolean validaAte(Optional<Vacina> vacina, LocalDate data) {
        return vacina.isPresent() && !Vacinas.validade(vacina.get()).isBefore(data);
    }
}
