package com.caiooliveira.petcheckin.service.regras;

import static com.caiooliveira.petcheckin.service.regras.Cenario.RELOGIO;
import static com.caiooliveira.petcheckin.service.regras.Cenario.antirrabicaEmDia;
import static com.caiooliveira.petcheckin.service.regras.Cenario.contexto;
import static com.caiooliveira.petcheckin.service.regras.Cenario.dadosComVacinas;
import static com.caiooliveira.petcheckin.service.regras.Cenario.dadosValidos;
import static com.caiooliveira.petcheckin.service.regras.Cenario.polivalente;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.caiooliveira.petcheckin.model.Especie;

class RegraPolivalenteTest {

    private static final String PENDENCIA = "A vacina polivalente (V8 ou V10) está ausente, vencida "
            + "ou vence durante a estadia. Atualize antes do check-in.";
    private static final LocalDate ABRIL_2026 = LocalDate.of(2026, 4, 15);

    private final RegraPolivalente regra = new RegraPolivalente(RELOGIO);

    @Test
    void passaComV10ParaCao() {
        assertThat(regra.avaliar(contexto(dadosValidos()))).isEmpty();
    }

    @Test
    void passaComV8ParaCao() {
        var dados = dadosComVacinas(antirrabicaEmDia(), polivalente("V8", ABRIL_2026, null));

        assertThat(regra.avaliar(contexto(dados))).isEmpty();
    }

    @Test
    void falhaComPolivalenteIncompativelComCao() {
        var dados = dadosComVacinas(antirrabicaEmDia(), polivalente("V4 (Felina)", ABRIL_2026, null));

        assertThat(regra.avaliar(contexto(dados))).containsExactly("Vacina incompatível com a espécie.");
    }

    @Test
    void passaComV4ParaGato() {
        var dados = dadosComVacinas(antirrabicaEmDia(), polivalente("V4 (Felina)", ABRIL_2026, null));

        assertThat(regra.avaliar(contexto(dados, Especie.GATO, 95.0, 64))).isEmpty();
    }

    @Test
    void falhaSemPolivalente() {
        assertThat(regra.avaliar(contexto(dadosComVacinas(antirrabicaEmDia())))).containsExactly(PENDENCIA);
    }

    @Test
    void falhaComPolivalenteAplicadaHa12MesesEUmDia() {
        var aplicacao = Cenario.HOJE.minusMonths(12).minusDays(1);
        var dados = dadosComVacinas(antirrabicaEmDia(), polivalente("V10", aplicacao, null));

        assertThat(regra.avaliar(contexto(dados))).containsExactly(PENDENCIA);
    }

    @Test
    void falhaComPolivalenteAplicadaHaMaisDe12Meses() {
        var dados = dadosComVacinas(antirrabicaEmDia(), polivalente("V10", LocalDate.of(2025, 10, 1), null));

        assertThat(regra.avaliar(contexto(dados))).containsExactly(PENDENCIA);
    }
}
