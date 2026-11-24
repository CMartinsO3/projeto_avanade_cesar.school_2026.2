# 🐾 PetCheck-in: Validação Inteligente de Documentos

Projeto do semestre da **Mentoria Avanade** (ADS, CESAR School).

API que recebe a **carteira de vacinação** de um pet, lê o documento com **OCR + IA**
e aplica regras de negócio para dizer se o pet está **APTO**, **NÃO APTO** ou **EM ANÁLISE MANUAL**
para a hospedagem.

## 🚧 Status
Em desenvolvimento.

## 🛠️ Tecnologias
- Java 21 + Spring Boot 4 (Spring Web MVC, Spring Data JPA, Bean Validation)
- PostgreSQL 17 (Docker)
- Tesseract OCR 5 (via linha de comando) + Apache PDFBox (PDF → imagem)
- Google Gemini (`gemini-2.5-flash`) via API REST (`RestClient`) para extrair os dados em JSON
- Expressões regulares (Regex) como alternativa quando a IA não está disponível
- Jackson 3 (conversão de JSON)

## ▶️ Como rodar

**Pré-requisitos:** Java 21, Docker e Tesseract com o idioma português
(`sudo apt install tesseract-ocr tesseract-ocr-por`).

1. Suba o banco de dados (PostgreSQL):
```bash
docker compose up -d
```

2. *(Opcional)* Configure a chave da IA. Sem ela, a extração usa Regex.
```bash
cp .env.example .env
# edite o .env e troque "coloque-sua-chave-aqui" pela sua chave do Gemini
# (gere em https://aistudio.google.com/apikey)
```
O arquivo `.env` está no `.gitignore`: a chave nunca vai para o GitHub.

3. Rode a aplicação:
```bash
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`.
O banco fica disponível em `localhost:5433` (usuário/senha/banco: `petcheckin`).

Para desligar o banco: `docker compose down` (os dados são mantidos).

## 📁 Estrutura do projeto
```
src/main/java/com/caiooliveira/petcheckin/
├── config/       → configurações (ex.: o relógio do sistema, Clock)
├── controller/   → recebe as requisições HTTP e devolve as respostas
├── dto/          → formatos dos dados que entram e saem da API
├── exception/    → exceções próprias e tratamento global de erros
├── service/      → regras de negócio
│   └── regras/   → uma classe por regra de validação (R1, R2, R3, R4, R5, R9)
├── repository/   → acesso ao banco de dados (Spring Data JPA)
└── model/        → entidades salvas no banco (Pet, Reserva, Documento, Validacao) e enums

docs/exemplos/    → carteiras fictícias para testar cada resultado
```

## 🔌 Endpoints
| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/health` | Verifica se a API está no ar |
| POST | `/api/pets` | Cadastra um pet |
| GET | `/api/pets` | Lista todos os pets |
| GET | `/api/pets/{id}` | Busca um pet pelo id |
| POST | `/api/reservas` | Cadastra uma reserva (status inicial: `AGUARDANDO_DOCUMENTOS`) |
| GET | `/api/reservas` | Lista todas as reservas |
| GET | `/api/reservas/{id}` | Busca uma reserva pelo id |
| POST | `/api/reservas/{id}/documentos` | Envia um documento (multipart: `arquivo` + `tipo`) |
| GET | `/api/reservas/{id}/documentos` | Lista os documentos da reserva |
| POST | `/api/reservas/{id}/validacao` | Valida a reserva com a carteira processada mais recente |
| GET | `/api/reservas/{id}/validacao` | Mostra o último resultado da validação |

### Exemplo: cadastrar um pet
```bash
curl -X POST http://localhost:8080/api/pets \
  -H "Content-Type: application/json" \
  -d '{"nome":"Thor","nomeTutor":"Caio Oliveira","especie":"CACHORRO","raca":"Golden Retriever","dataNascimento":"2022-03-15"}'
```

### Exemplo: cadastrar uma reserva
```bash
curl -X POST http://localhost:8080/api/reservas \
  -H "Content-Type: application/json" \
  -d '{"petId":1,"dataCheckin":"2026-12-01","dataCheckout":"2026-12-05"}'
```
### Exemplo: enviar a carteira de vacinação
```bash
curl -X POST http://localhost:8080/api/reservas/1/documentos \
  -F "tipo=CARTEIRA_VACINACAO" \
  -F "arquivo=@carteira_thor.png;type=image/png"
```
Formatos aceitos: PDF, PNG e JPG (máx. 10MB).

Depois do OCR, os dados da carteira são extraídos automaticamente
(com IA ou Regex — campo `metodoExtracao`). Resposta (resumida):
```json
{
  "id": 5,
  "status": "PROCESSADO",
  "confiancaOcr": 94.8,
  "palavrasOcr": 64,
  "metodoExtracao": "REGEX",
  "dadosExtraidos": {
    "nomePet": "Thor",
    "especie": "CACHORRO",
    "raca": "Golden Retriever",
    "dataNascimento": "2022-03-15",
    "nomeTutor": "Caio Oliveira",
    "crmvVeterinario": "CRMV-PE 12345",
    "vacinas": [
      { "nome": "Antirrábica", "tipo": "ANTIRRABICA", "dataAplicacao": "2026-03-10", "proximaDose": "2027-03-10" },
      { "nome": "V10 (Polivalente)", "tipo": "POLIVALENTE", "dataAplicacao": "2026-04-15", "proximaDose": "2027-04-15" }
    ]
  }
}
```

### Exemplo: validar a reserva
```bash
curl -X POST http://localhost:8080/api/reservas/1/validacao
```
Resposta (resumida):
```json
{
  "aprovado": false,
  "status": "NAO_APTO",
  "regrasAtendidas": ["R1 - Documento legível", "R2 - Campos obrigatórios preenchidos", "..."],
  "pendencias": ["A vacina antirrábica está ausente, vencida ou vence durante a estadia. Atualize antes do check-in."],
  "etapas": [
    { "nome": "Upload", "concluida": true, "detalhe": "carteira_thor_vencida.png" },
    { "nome": "OCR", "concluida": true, "detalhe": "Confiança de 95.3%" },
    { "nome": "Extração", "concluida": true, "detalhe": "Dados extraídos com REGEX" },
    { "nome": "Validação", "concluida": true, "detalhe": "NAO_APTO" }
  ],
  "statusReserva": "RECUSADA"
}
```
Se a reserva ainda não tiver carteira processada, a API responde `400`.

### Regras de validação (MVP)
| Regra | O que verifica | Se falhar |
|---|---|---|
| R1 | Documento legível: confiança do OCR ≥ 70% e ≥ 20 palavras (`app.regras.*`) | NÃO APTO |
| R2 | Nome do pet, tutor, CRMV e ao menos uma vacina com data de aplicação | NÃO APTO |
| R3 | Antirrábica aplicada nos últimos 12 meses (e não no futuro) | NÃO APTO |
| R4 | Polivalente compatível com a espécie (cão: V8 ou V10) aplicada nos últimos 12 meses | NÃO APTO |
| R5 | Antirrábica e polivalente válidas até o check-out (próxima dose, ou aplicação + 12 meses) | NÃO APTO |
| R9 | Nome do pet no documento igual ao da reserva (sem acentos, maiúsculas e espaços) | EM ANÁLISE MANUAL |

Resultado → status da reserva: **APTO → `CONFIRMADA`**, **NÃO APTO → `RECUSADA`**,
**EM ANÁLISE MANUAL → `EM_ANALISE_MANUAL`**.

### Carteiras de exemplo (`docs/exemplos/`)
Todas fictícias, para a reserva do Thor (check-out em 05/12/2026):

| Arquivo | Resultado esperado |
|---|---|
| `carteira_thor.png` / `carteira_thor.pdf` | APTO |
| `carteira_thor_vencida.png` | NÃO APTO (antirrábica aplicada há mais de 12 meses) |
| `carteira_thor_vence_na_estadia.png` | NÃO APTO (antirrábica vence durante a estadia) |
| `carteira_thor_nome_diferente.png` | EM ANÁLISE MANUAL (nome "Thorzinho") |

Para gerar de novo (ex.: mudar datas): `python3 docs/exemplos/gerar_carteiras.py`.

### Formato dos erros
Todos os erros seguem o mesmo formato:
```json
{ "status": 404, "erro": "Pet não encontrado com id 999" }
```
Erros de validação também informam os campos com problema:
```json
{ "status": 400, "erro": "Dados inválidos", "campos": { "nome": "O nome do pet é obrigatório" } }
```


## 🧠 Decisões técnicas
- **Datas no padrão ISO 8601 (`aaaa-mm-dd`)**: é o formato universal entre sistemas
  (Java, PostgreSQL, JavaScript, IA), não é ambíguo e ordena corretamente.
  A conversão para `dd/mm/aaaa` é feita apenas na interface (front-end).
- **DTOs separados das entidades**: a API não expõe as entidades do banco;
  isso evita que o cliente altere campos indevidos (como o `id`) e concentra as validações de entrada.
- **Prefixo `/api` nas rotas**: separa os endpoints da API das páginas do front-end,
  que serão servidas pelo mesmo servidor.
- **Tratamento global de erros (`@RestControllerAdvice`)**: todas as respostas de erro têm o mesmo
  formato, em português e sem detalhes internos (stack trace), que ficam apenas no log do servidor.
  Os services lançam exceções de negócio e não conhecem HTTP.
- **Regras de entrada × regras de negócio**: validações de formato (campo obrigatório, data no passado)
  ficam nos DTOs com Bean Validation; regras que dependem do negócio ou comparam campos
  (ex.: check-out posterior ao check-in) ficam nos services e lançam `RegraNegocioException`.
- **Arquivos no disco, metadados no banco**: o arquivo é salvo na pasta `uploads/` e o banco guarda
  apenas o caminho e as informações do envio, mantendo o banco leve (padrão usado com storages como S3/Azure Blob).
- **Nome do arquivo gerado com UUID**: evita que um arquivo sobrescreva outro e impede nomes maliciosos
  (ex.: `../../`). O caminho interno não é exposto na API.
- **Rota aninhada `/api/reservas/{id}/documentos`**: deixa explícito que todo documento pertence a uma reserva.
- **Tesseract chamado pela linha de comando (`ProcessBuilder`)**: evita os problemas de bibliotecas nativas
  do Tess4J no Linux e segue o modelo da mentoria (`app.ocr.comando`). O formato TSV devolve a confiança
  de cada palavra, usada como base da regra R1 (documento legível).
- **PDF convertido em imagem com PDFBox (300 DPI, tons de cinza)**: o Tesseract só lê imagens.
- **Falha no OCR não perde o envio**: o documento é salvo antes do processamento e, se o OCR falhar,
  fica com status `FALHA_PROCESSAMENTO`. O processamento é síncrono no MVP; em produção, o ideal
  seria uma fila assíncrona.
- **IA com fallback por Regex**: se a chave do Gemini não estiver configurada, ou se a IA falhar
  (sem internet, limite de uso, timeout, resposta inválida), o sistema registra um aviso no log e
  extrai os dados com expressões regulares. O fluxo nunca para por causa da IA, e a resposta informa
  qual método foi usado (`IA` ou `REGEX`).
- **Interface `ExtratorDadosCarteira`**: Gemini e Regex implementam o mesmo contrato. Para trocar de
  IA (ex.: Azure OpenAI), basta criar outra implementação, sem mexer nas regras de negócio.
- **JSON mode + `temperature = 0`**: o Gemini é obrigado a responder em JSON (`responseMimeType`)
  e com temperatura zero, que dá respostas mais previsíveis. O prompt manda usar `null` quando a
  informação não estiver no texto, para a IA não inventar dados.
- **Timeouts na chamada à IA (10 s para conectar, 60 s para responder)**: uma IA lenta não trava o upload.
- **Chave da IA fora do código**: lida da variável `GEMINI_API_KEY` (ou do arquivo `.env`, ignorado
  pelo Git). O repositório tem apenas o `.env.example`, um modelo sem a chave.
- **Dados extraídos guardados como JSON (`TEXT`)**: o formato pode evoluir (novos campos, outros
  documentos) sem precisar mudar tabelas no MVP.
- **LGPD**: com a IA ligada, o texto da carteira (nome do tutor e do pet) é enviado a um serviço
  externo. Em produção, isso exigiria consentimento do tutor, contrato com o provedor e,
  de preferência, a anonimização dos dados pessoais antes do envio.
- **Uma classe por regra (interface `RegraValidacao`)**: cada regra é pequena, fácil de entender
  e de testar sozinha. O `ValidacaoService` recebe a lista de todas as regras por injeção de
  dependência; para criar a R6, basta criar uma nova classe, sem alterar o service.
- **`Clock` injetado**: as regras perguntam a data de hoje ao `Clock`, e não direto ao computador.
  Nos testes, um `Clock.fixed(...)` "congela" a data, e o resultado não muda com o passar do tempo.
- **Espécie e check-out vêm do cadastro, não do documento**: o que vale é o que foi reservado.
  O documento é a prova que está sendo conferida.
- **Nome divergente não reprova (R9)**: a carteira pode trazer um apelido ("Thorzinho"),
  então o caso vai para uma pessoa analisar, em vez de recusar a reserva automaticamente.
- **Pendências sem repetição**: R3 e R5 podem apontar o mesmo problema da antirrábica;
  a mensagem aparece uma vez só para o tutor.
- **Histórico de validações**: cada validação é salva (status, regras atendidas, pendências,
  data/hora e documento usado). O `GET` devolve a mais recente, e as anteriores ficam para auditoria.
- **Validação separada do upload**: o tutor pode enviar a carteira de novo e validar outra vez;
  a validação sempre usa a carteira processada mais recente.

## 👤 Autor
Caio Martins Oliveira