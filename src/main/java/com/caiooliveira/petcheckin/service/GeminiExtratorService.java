package com.caiooliveira.petcheckin.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.caiooliveira.petcheckin.dto.DadosCarteira;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Service
public class GeminiExtratorService implements ExtratorDadosCarteira {

    private static final Duration TIMEOUT_CONEXAO = Duration.ofSeconds(10);
    private static final Duration TIMEOUT_RESPOSTA = Duration.ofSeconds(60);

    private static final String PROMPT = """
            Você é um assistente que extrai dados de carteiras de vacinação de pets.
            Leia o texto abaixo, obtido por OCR (pode conter erros de leitura), e responda
            SOMENTE com um JSON neste formato:
            {
              "nomePet": "texto",
              "especie": "CACHORRO" ou "GATO",
              "raca": "texto",
              "dataNascimento": "aaaa-mm-dd",
              "nomeTutor": "texto",
              "crmvVeterinario": "CRMV-UF 00000",
              "vacinas": [
                {
                  "nome": "nome como está no documento",
                  "tipo": "ANTIRRABICA" ou "POLIVALENTE" ou "OUTRA",
                  "dataAplicacao": "aaaa-mm-dd",
                  "proximaDose": "aaaa-mm-dd"
                }
              ]
            }
            Regras:
            - Datas sempre no formato aaaa-mm-dd.
            - Vacinas V4, V5, V8, V10 ou "polivalente" são do tipo POLIVALENTE; antirrábica/raiva é ANTIRRABICA.
            - Se uma informação não estiver no texto, use null. Não invente informações.

            Texto do documento:
            """;

    private final RestClient restClient;
    private final JsonMapper jsonMapper;
    private final String apiKey;
    private final String modelo;

    public GeminiExtratorService(JsonMapper jsonMapper,
                                 @Value("${app.ia.gemini.api-key}") String apiKey,
                                 @Value("${app.ia.gemini.modelo}") String modelo,
                                 @Value("${app.ia.gemini.url}") String url) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(TIMEOUT_CONEXAO);
        fabrica.setReadTimeout(TIMEOUT_RESPOSTA);

        this.restClient = RestClient.builder()
                .baseUrl(url)
                .requestFactory(fabrica)
                .build();
        this.jsonMapper = jsonMapper;
        this.apiKey = apiKey;
        this.modelo = modelo;
    }

    public boolean estaConfigurado() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public DadosCarteira extrair(String texto) {
        Map<String, Object> corpo = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", PROMPT + texto)))),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "temperature", 0));

        String resposta = restClient.post()
                .uri("/{modelo}:generateContent", modelo)
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(corpo)
                .retrieve()
                .body(String.class);

        JsonNode textoGerado = jsonMapper.readTree(resposta).at("/candidates/0/content/parts/0/text");
        if (textoGerado.isMissingNode() || textoGerado.isNull()) {
            throw new IllegalStateException("A resposta do Gemini não trouxe o JSON esperado");
        }
        return jsonMapper.readValue(textoGerado.asString(), DadosCarteira.class);
    }
}
