package com.caiooliveira.petcheckin.service.regras;

import static com.caiooliveira.petcheckin.service.regras.Cenario.contexto;
import static com.caiooliveira.petcheckin.service.regras.Cenario.dados;
import static com.caiooliveira.petcheckin.service.regras.Cenario.dadosValidos;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.caiooliveira.petcheckin.model.StatusValidacao;

class RegraNomePetTest {

    private final RegraNomePet regra = new RegraNomePet();

    @Test
    void passaComONomeIgual() {
        assertThat(regra.avaliar(contexto(dadosValidos()))).isEmpty();
    }

    @Test
    void ignoraAcentosMaiusculasEEspacos() {
        var dados = dados("  tHÓR ", "Caio Oliveira", "CRMV-PE 12345", dadosValidos().vacinas());

        assertThat(regra.avaliar(contexto(dados))).isEmpty();
    }

    @Test
    void falhaComApelido() {
        var dados = dados("Thorzinho", "Caio Oliveira", "CRMV-PE 12345", dadosValidos().vacinas());

        assertThat(regra.avaliar(contexto(dados)))
                .containsExactly("Nome do pet diferente do cadastrado: análise manual.");
    }

    @Test
    void falhaQuandoODocumentoNaoTemNome() {
        var dados = dados(null, "Caio Oliveira", "CRMV-PE 12345", dadosValidos().vacinas());

        assertThat(regra.avaliar(contexto(dados))).hasSize(1);
    }

    @Test
    void mandaParaAnaliseManualEmVezDeReprovar() {
        assertThat(regra.statusSeFalhar()).isEqualTo(StatusValidacao.EM_ANALISE_MANUAL);
    }

    @Test
    void normalizaRemovendoAcentosEspacosEDeixandoEmMaiusculas() {
        assertThat(RegraNomePet.normalizar("Thór  Júnior")).isEqualTo("THORJUNIOR");
    }
}
