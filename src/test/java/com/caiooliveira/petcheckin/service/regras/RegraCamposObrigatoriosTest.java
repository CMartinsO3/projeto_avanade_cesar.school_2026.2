package com.caiooliveira.petcheckin.service.regras;

import static com.caiooliveira.petcheckin.service.regras.Cenario.antirrabica;
import static com.caiooliveira.petcheckin.service.regras.Cenario.contexto;
import static com.caiooliveira.petcheckin.service.regras.Cenario.dados;
import static com.caiooliveira.petcheckin.service.regras.Cenario.dadosValidos;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class RegraCamposObrigatoriosTest {

    private final RegraCamposObrigatorios regra = new RegraCamposObrigatorios();

    @Test
    void passaComTodosOsCamposPreenchidos() {
        assertThat(regra.avaliar(contexto(dadosValidos()))).isEmpty();
    }

    @Test
    void falhaSemCrmv() {
        var dados = dados("Thor", "Caio Oliveira", null, dadosValidos().vacinas());

        assertThat(regra.avaliar(contexto(dados)))
                .containsExactly("Está faltando uma informação obrigatória: CRMV do veterinário.");
    }

    @Test
    void falhaQuandoNenhumaVacinaTemDataDeAplicacao() {
        var dados = dados("Thor", "Caio Oliveira", "CRMV-PE 12345", List.of(antirrabica(null, null)));

        assertThat(regra.avaliar(contexto(dados)))
                .containsExactly("Está faltando uma informação obrigatória: vacina com data de aplicação.");
    }

    @Test
    void listaTodosOsCamposQueFaltamNumaMensagemSo() {
        var dados = dados(" ", null, "CRMV-PE 12345", List.of());

        assertThat(regra.avaliar(contexto(dados))).containsExactly(
                "Está faltando uma informação obrigatória: nome do pet, nome do tutor, vacina com data de aplicação.");
    }
}
