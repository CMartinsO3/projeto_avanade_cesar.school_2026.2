package com.caiooliveira.petcheckin.service.regras;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.caiooliveira.petcheckin.dto.Vacina;

/** R3 - antirrábica presente e aplicada há no máximo 12 meses (e não no futuro). */
@Component
public class RegraAntirrabica implements RegraValidacao {

    private final Clock clock;

    public RegraAntirrabica(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String codigo() {
        return "R3";
    }

    @Override
    public String descricao() {
        return "Antirrábica aplicada nos últimos 12 meses";
    }

    @Override
    public List<String> avaliar(ContextoValidacao contexto) {
        LocalDate hoje = LocalDate.now(clock);
        Optional<Vacina> antirrabica = Vacinas.antirrabicaMaisRecente(contexto.dados().vacinas());

        if (antirrabica.isPresent() && Vacinas.aplicadaNosUltimos12Meses(antirrabica.get(), hoje)) {
            return List.of();
        }
        return List.of(Vacinas.MENSAGEM_VACINA.formatted(Vacinas.NOME_ANTIRRABICA));
    }
}
