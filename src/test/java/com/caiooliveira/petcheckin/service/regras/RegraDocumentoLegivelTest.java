package com.caiooliveira.petcheckin.service.regras;

import static com.caiooliveira.petcheckin.service.regras.Cenario.contexto;
import static com.caiooliveira.petcheckin.service.regras.Cenario.dadosValidos;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.caiooliveira.petcheckin.model.Especie;

class RegraDocumentoLegivelTest {

    private final RegraDocumentoLegivel regra = new RegraDocumentoLegivel(70, 20);

    @Test
    void passaQuandoConfiancaEPalavrasEstaoAcimaDoMinimo() {
        assertThat(regra.avaliar(contexto(dadosValidos(), Especie.CACHORRO, 95.0, 64))).isEmpty();
    }

    @Test
    void passaExatamenteNoLimite() {
        assertThat(regra.avaliar(contexto(dadosValidos(), Especie.CACHORRO, 70.0, 20))).isEmpty();
    }

    @Test
    void falhaComConfiancaBaixa() {
        assertThat(regra.avaliar(contexto(dadosValidos(), Especie.CACHORRO, 69.9, 64)))
                .containsExactly("Não conseguimos ler o documento. Envie uma foto mais nítida e sem cortes.");
    }

    @Test
    void falhaComPoucasPalavras() {
        assertThat(regra.avaliar(contexto(dadosValidos(), Especie.CACHORRO, 95.0, 19))).hasSize(1);
    }

    @Test
    void falhaQuandoOcrNaoFoiFeito() {
        assertThat(regra.avaliar(contexto(dadosValidos(), Especie.CACHORRO, null, null))).hasSize(1);
    }
}
