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
├── service/      → regras de negócio (em breve)
├── repository/   → acesso ao banco de dados (em breve)
└── model/        → entidades salvas no banco (Pet, Reserva)
```

## 🔌 Endpoints
| Método | Rota | Descrição |
|---|---|---|
| GET | `/health` | Verifica se a API está no ar |

## 👤 Autor
Caio Martins Oliveira