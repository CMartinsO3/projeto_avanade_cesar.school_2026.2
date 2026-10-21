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
```bash
./mvnw spring-boot:run
```
A aplicação sobe em `http://localhost:8080`.

## 📁 Estrutura do projeto
```
src/main/java/com/caiooliveira/petcheckin/
├── controller/   → recebe as requisições HTTP e devolve as respostas
├── dto/          → formatos dos dados que entram e saem da API
├── service/      → regras de negócio (em breve)
├── repository/   → acesso ao banco de dados (em breve)
└── model/        → entidades salvas no banco (em breve)
```

## 🔌 Endpoints
| Método | Rota | Descrição |
|---|---|---|
| GET | `/health` | Verifica se a API está no ar |

## 👤 Autor
Caio Martins Oliveira