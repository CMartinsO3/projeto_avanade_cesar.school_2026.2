package com.caiooliveira.petcheckin.service.regras;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import com.caiooliveira.petcheckin.dto.DadosCarteira;
import com.caiooliveira.petcheckin.dto.Vacina;
import com.caiooliveira.petcheckin.model.Documento;
import com.caiooliveira.petcheckin.model.Especie;
import com.caiooliveira.petcheckin.model.Pet;
import com.caiooliveira.petcheckin.model.Reserva;
import com.caiooliveira.petcheckin.model.TipoDocumento;
import com.caiooliveira.petcheckin.model.TipoVacina;

/**
 * Monta os dados dos testes. O cenário padrão é o Thor com tudo em dia:
 * hoje = 24/11/2026, reserva de 01/12/2026 a 05/12/2026.
 * Cada teste muda só o que interessa a ele.
 */
final class Cenario {

    static final LocalDate HOJE = LocalDate.of(2026, 11, 24);
    static final LocalDate CHECKIN = LocalDate.of(2026, 12, 1);
    static final LocalDate CHECKOUT = LocalDate.of(2026, 12, 5);

    /** Um relógio "parado" em HOJE: o resultado dos testes não muda com o passar dos dias. */
    static final Clock RELOGIO = Clock.fixed(
            HOJE.atStartOfDay(ZoneId.of("America/Recife")).toInstant(), ZoneId.of("America/Recife"));

    private Cenario() {
    }

    static Vacina antirrabica(LocalDate aplicacao, LocalDate proximaDose) {
        return new Vacina("Antirrábica", TipoVacina.ANTIRRABICA, aplicacao, proximaDose);
    }

    static Vacina polivalente(String nome, LocalDate aplicacao, LocalDate proximaDose) {
        return new Vacina(nome, TipoVacina.POLIVALENTE, aplicacao, proximaDose);
    }

    static Vacina antirrabicaEmDia() {
        return antirrabica(LocalDate.of(2026, 3, 10), LocalDate.of(2027, 3, 10));
    }

    static Vacina v10EmDia() {
        return polivalente("V10 (Polivalente)", LocalDate.of(2026, 4, 15), LocalDate.of(2027, 4, 15));
    }

    static DadosCarteira dados(String nomePet, String nomeTutor, String crmv, List<Vacina> vacinas) {
        return new DadosCarteira(nomePet, Especie.CACHORRO, "Golden Retriever",
                LocalDate.of(2022, 3, 15), nomeTutor, crmv, vacinas);
    }

    static DadosCarteira dadosComVacinas(Vacina... vacinas) {
        return dados("Thor", "Caio Oliveira", "CRMV-PE 12345", List.of(vacinas));
    }

    static DadosCarteira dadosValidos() {
        return dadosComVacinas(antirrabicaEmDia(), v10EmDia());
    }

    static ContextoValidacao contexto(DadosCarteira dados) {
        return contexto(dados, Especie.CACHORRO, 95.0, 64);
    }

    static ContextoValidacao contexto(DadosCarteira dados, Especie especieCadastrada,
                                      Double confiancaOcr, Integer palavrasOcr) {
        Pet pet = new Pet("Thor", "Caio Oliveira", especieCadastrada, "Golden Retriever", LocalDate.of(2022, 3, 15));
        Reserva reserva = new Reserva(pet, CHECKIN, CHECKOUT);
        Documento documento = new Documento(reserva, TipoDocumento.CARTEIRA_VACINACAO,
                "carteira.png", "uploads/teste.png", "image/png", 1000L);
        if (confiancaOcr != null && palavrasOcr != null) {
            documento.registrarOcr("texto", confiancaOcr, palavrasOcr);
        }
        return new ContextoValidacao(reserva, documento, dados);
    }
}
