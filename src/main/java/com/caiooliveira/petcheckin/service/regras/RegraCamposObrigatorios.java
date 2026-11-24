package com.caiooliveira.petcheckin.service.regras;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.caiooliveira.petcheckin.dto.DadosCarteira;

/** R2 - nome do pet, tutor, CRMV e ao menos uma vacina com data de aplicação. */
@Component
public class RegraCamposObrigatorios implements RegraValidacao {

    @Override
    public String codigo() {
        return "R2";
    }

    @Override
    public String descricao() {
        return "Campos obrigatórios preenchidos";
    }

    @Override
    public List<String> avaliar(ContextoValidacao contexto) {
        DadosCarteira dados = contexto.dados();
        List<String> faltando = new ArrayList<>();

        if (vazio(dados.nomePet())) {
            faltando.add("nome do pet");
        }
        if (vazio(dados.nomeTutor())) {
            faltando.add("nome do tutor");
        }
        if (vazio(dados.crmvVeterinario())) {
            faltando.add("CRMV do veterinário");
        }
        boolean temVacinaComData = dados.vacinas().stream()
                .anyMatch(vacina -> vacina.dataAplicacao() != null);
        if (!temVacinaComData) {
            faltando.add("vacina com data de aplicação");
        }

        if (faltando.isEmpty()) {
            return List.of();
        }
        return List.of("Está faltando uma informação obrigatória: " + String.join(", ", faltando) + ".");
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
