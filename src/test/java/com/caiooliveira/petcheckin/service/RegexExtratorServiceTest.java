package com.caiooliveira.petcheckin.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.caiooliveira.petcheckin.dto.DadosCarteira;
import com.caiooliveira.petcheckin.dto.Vacina;
import com.caiooliveira.petcheckin.model.Especie;
import com.caiooliveira.petcheckin.model.TipoVacina;

class RegexExtratorServiceTest {

    private final RegexExtratorService extrator = new RegexExtratorService();

    /**
     * Os dois arquivos têm o texto que o OcrService gerou de verdade a partir de
     * carteira_thor.png e carteira_thor.pdf. No PDF, a coluna "Veterinário" e os CRMVs
     * vão para o fim do texto, mas o resultado da extração tem que ser o mesmo.
     */
    @ParameterizedTest
    @ValueSource(strings = { "ocr/carteira_thor_png.txt", "ocr/carteira_thor_pdf.txt" })
    void extraiTodosOsDadosDaCarteiraDoThor(String arquivo) throws IOException {
        DadosCarteira dados = extrator.extrair(lerRecurso(arquivo));

        assertThat(dados.nomePet()).isEqualTo("Thor");
        assertThat(dados.especie()).isEqualTo(Especie.CACHORRO);
        assertThat(dados.raca()).isEqualTo("Golden Retriever");
        assertThat(dados.dataNascimento()).isEqualTo(LocalDate.of(2022, 3, 15));
        assertThat(dados.nomeTutor()).isEqualTo("Caio Oliveira");
        assertThat(dados.crmvVeterinario()).isEqualTo("CRMV-PE 12345");
        assertThat(dados.vacinas()).containsExactly(
                new Vacina("Antirrábica", TipoVacina.ANTIRRABICA,
                        LocalDate.of(2026, 3, 10), LocalDate.of(2027, 3, 10)),
                new Vacina("V10 (Polivalente)", TipoVacina.POLIVALENTE,
                        LocalDate.of(2026, 4, 15), LocalDate.of(2027, 4, 15)));
    }

    @Test
    void reconheceGatoPelaEspecieFelina() {
        assertThat(extrator.extrair("Espécie: Felina").especie()).isEqualTo(Especie.GATO);
    }

    @Test
    void padronizaOCrmvMesmoEscritoDeOutroJeito() {
        assertThat(extrator.extrair("crmv pe 9876").crmvVeterinario()).isEqualTo("CRMV-PE 9876");
    }

    @Test
    void classificaOsTiposDeVacina() {
        String texto = """
                Vacina contra raiva 01/02/2026 01/02/2027
                V8 01/02/2026 01/02/2027
                Gripe canina 01/02/2026 01/02/2027
                """;

        assertThat(extrator.extrair(texto).vacinas())
                .extracting(Vacina::tipo)
                .containsExactly(TipoVacina.ANTIRRABICA, TipoVacina.POLIVALENTE, TipoVacina.OUTRA);
    }

    @Test
    void dataImpossivelViraNull() {
        DadosCarteira dados = extrator.extrair("""
                Data de nascimento: 31/02/2022
                Antirrábica 31/02/2026 10/03/2027
                """);

        assertThat(dados.dataNascimento()).isNull();
        assertThat(dados.vacinas().getFirst().dataAplicacao()).isNull();
        assertThat(dados.vacinas().getFirst().proximaDose()).isEqualTo(LocalDate.of(2027, 3, 10));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "texto sem nenhum dado da carteira" })
    void textoSemDadosDevolveCamposVazios(String texto) {
        DadosCarteira dados = extrator.extrair(texto);

        assertThat(dados.nomePet()).isNull();
        assertThat(dados.crmvVeterinario()).isNull();
        assertThat(dados.vacinas()).isEmpty();
    }

    @Test
    void textoNuloNaoQuebra() {
        assertThat(extrator.extrair(null).vacinas()).isEmpty();
    }

    private String lerRecurso(String caminho) throws IOException {
        try (InputStream entrada = getClass().getClassLoader().getResourceAsStream(caminho)) {
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
