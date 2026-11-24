package com.caiooliveira.petcheckin.service.regras;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.caiooliveira.petcheckin.dto.Vacina;
import com.caiooliveira.petcheckin.model.Especie;
import com.caiooliveira.petcheckin.model.TipoVacina;

/**
 * Funções de apoio sobre vacinas, usadas por mais de uma regra (R3, R4 e R5).
 */
final class Vacinas {

    static final String MENSAGEM_VACINA =
            "A vacina %s está ausente, vencida ou vence durante a estadia. Atualize antes do check-in.";
    static final String NOME_ANTIRRABICA = "antirrábica";
    static final String NOME_POLIVALENTE = "polivalente (V8 ou V10)";

    private static final Pattern POLIVALENTE_CAO = Pattern.compile("(?i)\\bv\\s?(8|10)\\b");
    private static final Pattern POLIVALENTE_GATO = Pattern.compile("(?i)\\bv\\s?(3|4|5)\\b|felina");

    private Vacinas() {
    }

    static Optional<Vacina> antirrabicaMaisRecente(List<Vacina> vacinas) {
        return maisRecente(vacinas.stream()
                .filter(vacina -> vacina.tipo() == TipoVacina.ANTIRRABICA)
                .toList());
    }

    static List<Vacina> polivalentes(List<Vacina> vacinas) {
        return vacinas.stream()
                .filter(vacina -> vacina.tipo() == TipoVacina.POLIVALENTE)
                .toList();
    }

    static Optional<Vacina> polivalenteCompativelMaisRecente(List<Vacina> vacinas, Especie especie) {
        return maisRecente(polivalentes(vacinas).stream()
                .filter(vacina -> compativelComEspecie(vacina, especie))
                .toList());
    }

    static boolean compativelComEspecie(Vacina vacina, Especie especie) {
        if (vacina.nome() == null || especie == null) {
            return false;
        }
        Pattern padrao = especie == Especie.CACHORRO ? POLIVALENTE_CAO : POLIVALENTE_GATO;
        return padrao.matcher(vacina.nome()).find();
    }

    /** Aplicada nos últimos 12 meses e não no futuro. */
    static boolean aplicadaNosUltimos12Meses(Vacina vacina, LocalDate hoje) {
        LocalDate aplicacao = vacina.dataAplicacao();
        return !aplicacao.isAfter(hoje) && !aplicacao.isBefore(hoje.minusMonths(12));
    }

    /** Até quando a vacina vale: a próxima dose ou, se não houver, 12 meses após a aplicação. */
    static LocalDate validade(Vacina vacina) {
        if (vacina.proximaDose() != null) {
            return vacina.proximaDose();
        }
        return vacina.dataAplicacao().plusMonths(12);
    }

    private static Optional<Vacina> maisRecente(List<Vacina> vacinas) {
        return vacinas.stream()
                .filter(vacina -> vacina.dataAplicacao() != null)
                .max(Comparator.comparing(Vacina::dataAplicacao));
    }
}
