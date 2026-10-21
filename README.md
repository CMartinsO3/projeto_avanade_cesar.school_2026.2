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
├── service/      → regras de negócio
├── repository/   → acesso ao banco de dados (Spring Data JPA)
└── model/        → entidades salvas no banco (Pet, Reserva)
```

## 🔌 Endpoints
| Método | Rota | Descrição |
|---|---|---|
| GET | `/health` | Verifica se a API está no ar |
| POST | `/pets` | Cadastra um pet |
| GET | `/pets` | Lista todos os pets |
| GET | `/pets/{id}` | Busca um pet pelo id |

### Exemplo: cadastrar um pet
```bash
curl -X POST http://localhost:8080/pets \
  -H "Content-Type: application/json" \
  -d '{"nome":"Thor","nomeTutor":"Caio Oliveira","especie":"CACHORRO","raca":"Golden Retriever","dataNascimento":"2022-03-15"}'
```

## 🧠 Decisões técnicas
- **Datas no padrão ISO 8601 (`aaaa-mm-dd`)**: é o formato universal entre sistemas
  (Java, PostgreSQL, JavaScript, IA), não é ambíguo e ordena corretamente.
  A conversão para `dd/mm/aaaa` é feita apenas na interface (front-end).
- **DTOs separados das entidades**: a API não expõe as entidades do banco;
  isso evita que o cliente altere campos indevidos (como o `id`) e concentra as validações de entrada.

## 👤 Autor
Caio Martins Oliveira