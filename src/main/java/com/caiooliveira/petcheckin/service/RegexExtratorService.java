package com.caiooliveira.petcheckin.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.caiooliveira.petcheckin.dto.DadosCarteira;
import com.caiooliveira.petcheckin.dto.Vacina;
import com.caiooliveira.petcheckin.model.Especie;
import com.caiooliveira.petcheckin.model.TipoVacina;

@Service
public class RegexExtratorService implements ExtratorDadosCarteira {

    private static final Pattern NOME_PET = Pattern.compile("(?i)nome do pet:\\s*(.+)");
    private static final Pattern ESPECIE = Pattern.compile("(?i)esp[ée]cie:\\s*(.+)");
    private static final Pattern RACA = Pattern.compile("(?i)ra[çc]a:\\s*(.+)");
    private static final Pattern NASCIMENTO = Pattern.compile("(?i)nascimento:\\s*(\\d{2}/\\d{2}/\\d{4})");
    private static final Pattern NOME_TUTOR = Pattern.compile("(?i)nome do tutor:\\s*(.+)");
    private static final Pattern CRMV = Pattern.compile("(?i)CRMV[-\\s]*([A-Z]{2})\\s*(\\d{3,6})");
    private static final Pattern VACINA = Pattern.compile(
            "(?im)^(.+?)\\s+(\\d{2}/\\d{2}/\\d{4})\\s+(\\d{2}/\\d{2}/\\d{4})");

    private static final Pattern ANTIRRABICA = Pattern.compile("(?i)antirr|raiva");
    private static final Pattern POLIVALENTE = Pattern.compile("(?i)\\bv\\s?(4|5|8|10)\\b|polivalente");

    // "uuuu" + STRICT: rejeita datas impossíveis, como 31/02/2026
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    @Override
    public DadosCarteira extrair(String texto) {
        String conteudo = texto == null ? "" : texto;

        return new DadosCarteira(
                buscar(NOME_PET, conteudo),
                converterEspecie(buscar(ESPECIE, conteudo)),
                buscar(RACA, conteudo),
                converterData(buscar(NASCIMENTO, conteudo)),
                buscar(NOME_TUTOR, conteudo),
                buscarCrmv(conteudo),
                buscarVacinas(conteudo)
        );
    }

    private String buscar(Pattern padrao, String texto) {
        Matcher matcher = padrao.matcher(texto);
        if (!matcher.find()) {
            return null;
        }
        String valor = matcher.group(1).trim();
        return valor.isEmpty() ? null : valor;
    }

    private String buscarCrmv(String texto) {
        Matcher matcher = CRMV.matcher(texto);
        if (!matcher.find()) {
            return null;
        }
        return "CRMV-" + matcher.group(1).toUpperCase(Locale.ROOT) + " " + matcher.group(2);
    }

    private List<Vacina> buscarVacinas(String texto) {
        List<Vacina> vacinas = new ArrayList<>();
        Matcher matcher = VACINA.matcher(texto);
        while (matcher.find()) {
            String nome = matcher.group(1).trim();
            vacinas.add(new Vacina(
                    nome,
                    classificarVacina(nome),
                    converterData(matcher.group(2)),
                    converterData(matcher.group(3))
            ));
        }
        return vacinas;
    }

    private Especie converterEspecie(String valor) {
        if (valor == null) {
            return null;
        }
        String especie = valor.toLowerCase(Locale.ROOT);
        if (especie.contains("can") || especie.contains("cão") || especie.contains("cachorro")) {
            return Especie.CACHORRO;
        }
        if (especie.contains("fel") || especie.contains("gat")) {
            return Especie.GATO;
        }
        return null;
    }

    private TipoVacina classificarVacina(String nome) {
        if (ANTIRRABICA.matcher(nome).find()) {
            return TipoVacina.ANTIRRABICA;
        }
        if (POLIVALENTE.matcher(nome).find()) {
            return TipoVacina.POLIVALENTE;
        }
        return TipoVacina.OUTRA;
    }

    private LocalDate converterData(String data) {
        if (data == null) {
            return null;
        }
        try {
            return LocalDate.parse(data, FORMATO_DATA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
