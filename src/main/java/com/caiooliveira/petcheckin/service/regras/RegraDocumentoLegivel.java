package com.caiooliveira.petcheckin.service.regras;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.caiooliveira.petcheckin.model.Documento;

/** R1 - o OCR conseguiu ler o documento com confiança e quantidade de palavras suficientes. */
@Component
public class RegraDocumentoLegivel implements RegraValidacao {

    private final double confiancaMinima;
    private final int palavrasMinimas;

    public RegraDocumentoLegivel(@Value("${app.regras.confianca-minima}") double confiancaMinima,
                                 @Value("${app.regras.palavras-minimas}") int palavrasMinimas) {
        this.confiancaMinima = confiancaMinima;
        this.palavrasMinimas = palavrasMinimas;
    }

    @Override
    public String codigo() {
        return "R1";
    }

    @Override
    public String descricao() {
        return "Documento legível";
    }

    @Override
    public List<String> avaliar(ContextoValidacao contexto) {
        Documento documento = contexto.documento();
        boolean confiancaOk = documento.getConfiancaOcr() != null
                && documento.getConfiancaOcr() >= confiancaMinima;
        boolean palavrasOk = documento.getPalavrasOcr() != null
                && documento.getPalavrasOcr() >= palavrasMinimas;

        if (confiancaOk && palavrasOk) {
            return List.of();
        }
        return List.of("Não conseguimos ler o documento. Envie uma foto mais nítida e sem cortes.");
    }
}
