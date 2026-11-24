package com.caiooliveira.petcheckin.service.regras;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.caiooliveira.petcheckin.model.StatusValidacao;

/**
 * R9 - o nome do pet no documento é igual ao da reserva.
 * Se for diferente, não reprova: pode ser um apelido, então vai para análise manual.
 */
@Component
public class RegraNomePet implements RegraValidacao {

    @Override
    public String codigo() {
        return "R9";
    }

    @Override
    public String descricao() {
        return "Nome do pet igual ao da reserva";
    }

    @Override
    public List<String> avaliar(ContextoValidacao contexto) {
        String nomeDocumento = contexto.dados().nomePet();
        String nomeCadastrado = contexto.pet().getNome();

        if (nomeDocumento != null && normalizar(nomeDocumento).equals(normalizar(nomeCadastrado))) {
            return List.of();
        }
        return List.of("Nome do pet diferente do cadastrado: análise manual.");
    }

    @Override
    public StatusValidacao statusSeFalhar() {
        return StatusValidacao.EM_ANALISE_MANUAL;
    }

    /** "Thór  Júnior" → "THORJUNIOR": sem acentos, maiúsculo e sem espaços. */
    static String normalizar(String texto) {
        String semAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcentos.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }
}
