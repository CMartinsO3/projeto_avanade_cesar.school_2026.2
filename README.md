# 🐾 PetCheck-in: Validação Inteligente de Documentos

Projeto do semestre da **Mentoria Avanade** (ADS, CESAR School).

API que recebe a **carteira de vacinação** de um pet, lê o documento com **OCR + IA**
e aplica regras de negócio para dizer se o pet está **APTO** ou **NÃO APTO** para a hospedagem.

## 🚧 Status
Em desenvolvimento.

## 🛠️ Tecnologias (planejadas)
- Java 21 + Spring Boot 4
- PostgreSQL (Docker)
- Tesseract OCR + OpenCV
- Google Gemini (IA)

## ▶️ Como rodar

**Pré-requisitos:** Java 21 e Docker.

1. Suba o banco de dados (PostgreSQL):
```bash
docker compose up -d
```

2. Rode a aplicação:
```bash
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`.
O banco fica disponível em `localhost:5433` (usuário/senha/banco: `petcheckin`).

Para desligar o banco: `docker compose down` (os dados são mantidos).

## 📁 Estrutura do projeto
```
src/main/java/com/caiooliveira/petcheckin/
├── controller/   → recebe as requisições HTTP e devolve as respostas
├── dto/          → formatos dos dados que entram e saem da API
├── exception/    → exceções próprias e tratamento global de erros
├── service/      → regras de negócio
├── repository/   → acesso ao banco de dados (Spring Data JPA)
└── model/        → entidades salvas no banco (Pet, Reserva)
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

## 👤 Autor
Caio Martins Oliveira