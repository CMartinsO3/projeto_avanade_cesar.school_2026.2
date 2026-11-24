package com.caiooliveira.petcheckin.service.regras;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.caiooliveira.petcheckin.dto.Vacina;
import com.caiooliveira.petcheckin.model.Especie;

/** R4 - polivalente compatível com a espécie (cão: V8 ou V10) e aplicada há no máximo 12 meses. */
@Component
public class RegraPolivalente implements RegraValidacao {

    private final Clock clock;

    public RegraPolivalente(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String codigo() {
        return "R4";
    }

    @Override
    public String descricao() {
        return "Polivalente compatível com a espécie e aplicada nos últimos 12 meses";
    }

    @Override
    public List<String> avaliar(ContextoValidacao contexto) {
        LocalDate hoje = LocalDate.now(clock);
        List<Vacina> vacinas = contexto.dados().vacinas();
        Especie especie = contexto.pet().getEspecie();

        Optional<Vacina> compativel = Vacinas.polivalenteCompativelMaisRecente(vacinas, especie);
        if (compativel.isEmpty() && !Vacinas.polivalentes(vacinas).isEmpty()) {
            return List.of("Vacina incompatível com a espécie.");
        }
        if (compativel.isPresent() && Vacinas.aplicadaNosUltimos12Meses(compativel.get(), hoje)) {
            return List.of();
        }
        return List.of(Vacinas.MENSAGEM_VACINA.formatted(Vacinas.NOME_POLIVALENTE));
    }
}
