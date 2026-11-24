package com.caiooliveira.petcheckin.service.regras;

import static com.caiooliveira.petcheckin.service.regras.Cenario.CHECKOUT;
import static com.caiooliveira.petcheckin.service.regras.Cenario.antirrabica;
import static com.caiooliveira.petcheckin.service.regras.Cenario.antirrabicaEmDia;
import static com.caiooliveira.petcheckin.service.regras.Cenario.contexto;
import static com.caiooliveira.petcheckin.service.regras.Cenario.dadosComVacinas;
import static com.caiooliveira.petcheckin.service.regras.Cenario.dadosValidos;
import static com.caiooliveira.petcheckin.service.regras.Cenario.polivalente;
import static com.caiooliveira.petcheckin.service.regras.Cenario.v10EmDia;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class RegraValidadeEstadiaTest {

    private final RegraValidadeEstadia regra = new RegraValidadeEstadia();

    @Test
    void passaQuandoAsDuasVacinasValemAteDepoisDoCheckout() {
        assertThat(regra.avaliar(contexto(dadosValidos()))).isEmpty();
    }

    @Test
    void passaQuandoAProximaDoseEhNoDiaDoCheckout() {
        var dados = dadosComVacinas(antirrabica(LocalDate.of(2025, 12, 5), CHECKOUT), v10EmDia());

        assertThat(regra.avaliar(contexto(dados))).isEmpty();
    }

    @Test
    void falhaQuandoAAntirrabicaVenceDuranteAEstadia() {
        var dados = dadosComVacinas(antirrabica(LocalDate.of(2025, 12, 2), LocalDate.of(2026, 12, 2)), v10EmDia());

        assertThat(regra.avaliar(contexto(dados))).containsExactly(
                "A vacina antirrábica está ausente, vencida ou vence durante a estadia. Atualize antes do check-in.");
    }

    @Test
    void semProximaDoseUsaAplicacaoMais12Meses() {
        var venceAntes = dadosComVacinas(antirrabica(LocalDate.of(2025, 12, 4), null), v10EmDia());
        var venceDepois = dadosComVacinas(antirrabica(LocalDate.of(2025, 12, 6), null), v10EmDia());

        assertThat(regra.avaliar(contexto(venceAntes))).hasSize(1);
        assertThat(regra.avaliar(contexto(venceDepois))).isEmpty();
    }

    @Test
    void apontaAsDuasVacinasQuandoAmbasVencem() {
        var dados = dadosComVacinas(
                antirrabica(LocalDate.of(2025, 12, 1), LocalDate.of(2026, 12, 1)),
                polivalente("V10", LocalDate.of(2025, 12, 1), LocalDate.of(2026, 12, 1)));

        assertThat(regra.avaliar(contexto(dados))).hasSize(2);
    }

    @Test
    void falhaQuandoNaoHaPolivalenteCompativel() {
        assertThat(regra.avaliar(contexto(dadosComVacinas(antirrabicaEmDia())))).containsExactly(
                "A vacina polivalente (V8 ou V10) está ausente, vencida ou vence durante a estadia. "
                        + "Atualize antes do check-in.");
    }
}
