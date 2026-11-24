package com.caiooliveira.petcheckin.service.regras;

import static com.caiooliveira.petcheckin.service.regras.Cenario.HOJE;
import static com.caiooliveira.petcheckin.service.regras.Cenario.RELOGIO;
import static com.caiooliveira.petcheckin.service.regras.Cenario.antirrabica;
import static com.caiooliveira.petcheckin.service.regras.Cenario.contexto;
import static com.caiooliveira.petcheckin.service.regras.Cenario.dadosComVacinas;
import static com.caiooliveira.petcheckin.service.regras.Cenario.dadosValidos;
import static com.caiooliveira.petcheckin.service.regras.Cenario.v10EmDia;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class RegraAntirrabicaTest {

    private static final String PENDENCIA =
            "A vacina antirrábica está ausente, vencida ou vence durante a estadia. Atualize antes do check-in.";

    private final RegraAntirrabica regra = new RegraAntirrabica(RELOGIO);

    @Test
    void passaComAntirrabicaAplicadaHaMenosDe12Meses() {
        assertThat(regra.avaliar(contexto(dadosValidos()))).isEmpty();
    }

    @Test
    void passaComAntirrabicaAplicadaHaExatamente12Meses() {
        var dados = dadosComVacinas(antirrabica(HOJE.minusMonths(12), null), v10EmDia());

        assertThat(regra.avaliar(contexto(dados))).isEmpty();
    }

    @Test
    void falhaComAntirrabicaAplicadaHa12MesesEUmDia() {
        var dados = dadosComVacinas(antirrabica(HOJE.minusMonths(12).minusDays(1), null), v10EmDia());

        assertThat(regra.avaliar(contexto(dados))).containsExactly(PENDENCIA);
    }

    @Test
    void falhaComAntirrabicaAplicadaHaMaisDe12Meses() {
        var dados = dadosComVacinas(antirrabica(LocalDate.of(2025, 2, 10), LocalDate.of(2026, 2, 10)), v10EmDia());

        assertThat(regra.avaliar(contexto(dados))).containsExactly(PENDENCIA);
    }

    @Test
    void falhaComDataDeAplicacaoNoFuturo() {
        var dados = dadosComVacinas(antirrabica(HOJE.plusDays(1), null), v10EmDia());

        assertThat(regra.avaliar(contexto(dados))).containsExactly(PENDENCIA);
    }

    @Test
    void falhaSemAntirrabica() {
        assertThat(regra.avaliar(contexto(dadosComVacinas(v10EmDia())))).containsExactly(PENDENCIA);
    }

    @Test
    void consideraADoseMaisRecenteQuandoHaMaisDeUma() {
        var dados = dadosComVacinas(
                antirrabica(LocalDate.of(2024, 3, 10), LocalDate.of(2025, 3, 10)),
                antirrabica(LocalDate.of(2026, 3, 10), LocalDate.of(2027, 3, 10)),
                v10EmDia());

        assertThat(regra.avaliar(contexto(dados))).isEmpty();
    }
}
