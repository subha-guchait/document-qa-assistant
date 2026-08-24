# Document Q&A Assistant

A multi-tenant Retrieval-Augmented Generation (RAG) backend built with
Java 21, Spring Boot, Spring AI, PostgreSQL, and pgvector.

The application allows tenants to upload documents, asynchronously process
them into chunks and embeddings, retrieve relevant content using vector
similarity search, and ask grounded questions through a conversational chat
API.

The system supports conversation memory, token-budgeted history, source
citations, document deletion, idempotent ingestion, and tenant-scoped
retrieval.

---

## Tech Stack

- Java 21
- Spring Boot
- Spring AI
- PostgreSQL 16
- pgvector
- Flyway
- Maven
- Docker / Docker Compose
- Google Gemini

### AI Models

Default embedding model:

- `gemini-embedding-001`
- 1536 dimensions

Default chat model:

- `gemini-2.5-flash`

The application also has provider configuration support for other embedding
providers such as OpenAI and Ollama.

---

# Architecture

The application is divided into separate modules based on responsibility.

```text
document_qa_assistant
│
├── common
│   ├── config
│   └── exception
│
├── document
│   ├── controller
│   ├── dto
│   ├── model
│   ├── repository
│   ├── service
│   └── exception
│
├── ingestion
│   ├── extraction
│   ├── chunking
│   ├── embedding
│   └── service
│
├── retrieval
│   ├── model
│   ├── repository
│   └── service
│
├── chat
│   ├── controller
│   ├── dto
│   ├── prompt
│   └── service
│
└── conversation
    ├── controller
    ├── dto
    ├── model
    ├── repository
    └── service
```

---

# System Flow

## Document ingestion flow

```text
Client
  │
  │ POST /api/v1/documents
  ▼
DocumentController
  │
  ▼
DocumentService
  │
  ├── Validate file
  ├── Calculate SHA-256
  ├── Check duplicate
  ├── Store document
  └── Mark PROCESSING
          │
          ▼
    IngestionService
          │
          ▼
    Text Extraction
          │
          ▼
       Chunking
          │
          ▼
    Batch Embeddings
          │
          ▼
 PostgreSQL + pgvector
          │
          ▼
     Mark READY
```

Document processing is asynchronous, so the upload endpoint returns
`202 Accepted` while ingestion continues in the background.

---

## Query flow

```text
Client
  │
  │ POST /api/v1/chat
  ▼
ChatController
  │
  ▼
ChatService
  │
  ├── Load/create conversation
  │
  ├── Load bounded conversation history
  │
  ├── Save user message
  │
  ▼
RetrievalService
  │
  ├── Generate query embedding
  ├── Tenant filtering
  ├── Category filtering
  ├── Vector similarity search
  ├── Top-K
  └── Similarity threshold
          │
          ▼
    Retrieved chunks
          │
          ▼
     PromptBuilder
          │
          ├── Conversation history
          ├── Retrieved documents
          └── Current question
          │
          ▼
       Chat Model
          │
          ▼
   Assistant response
          │
          ├── Save message
          └── Save message sources
          │
          ▼
      ChatResponse
```

---

# Running Locally

## Prerequisites

- Java 21+
- Maven or Maven Wrapper
- PostgreSQL with pgvector
- Docker / Docker Compose
- Google Gemini API key

---

## Environment Variables

Example `.env`:

```env
POSTGRES_DB=document_qa
POSTGRES_USER=document_qa
POSTGRES_PASSWORD=document_qa

POSTGRES_HOST_PORT=5433

DB_URL=jdbc:postgresql://localhost:5433/document_qa
DB_USERNAME=document_qa
DB_PASSWORD=document_qa

GEMINI_API_KEY=your-api-key

GEMINI_CHAT_MODEL=gemini-2.5-flash
GEMINI_EMBEDDING_MODEL=gemini-embedding-001
GEMINI_EMBEDDING_DIMENSIONS=1536

APP_PORT=8080

RETRIEVAL_TOP_K=5
RETRIEVAL_SIMILARITY_THRESHOLD=0.5

CONVERSATION_MAX_HISTORY_TURNS=6
CONVERSATION_MAX_HISTORY_TOKENS=3000

STORAGE_PATH=./uploads
```

Do not commit the `.env` file or API keys.

---

## Start PostgreSQL

The project includes a Docker Compose configuration for PostgreSQL with
pgvector.

```bash
docker compose up -d postgres
```

Verify that PostgreSQL is healthy:

```bash
docker compose ps
```

---

## Run the application

Using Maven Wrapper:

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### Linux/macOS

```bash
./mvnw spring-boot:run
```

Flyway automatically applies the database migrations when the application
starts.

---

## Run tests

Windows:

```powershell
.\mvnw.cmd clean test
```

Linux/macOS:

```bash
./mvnw clean test
```

---

# API

## Interactive API Documentation (Swagger)

The application provides an interactive Swagger UI to explore and test the endpoints.

Once the application is running locally, navigate to:

```text
http://localhost:8080/swagger-ui.html
```
*(Note: If you changed `APP_PORT` in your `.env` file, for example to 8081, use that port instead: `http://localhost:8081/swagger-ui.html`)*

Or for the raw OpenAPI JSON specification:

```text
http://localhost:8080/v3/api-docs
```

---

## Upload document

```http
POST /api/v1/documents
X-Tenant-Id: tenant-k
Content-Type: multipart/form-data
```

The upload endpoint accepts:

- PDF
- DOCX
- TXT
- Markdown

Example response:

```json
{
  "id": "document-uuid",
  "status": "PROCESSING"
}
```

The endpoint returns `202 Accepted` because ingestion is asynchronous.

---

## List documents

```http
GET /api/v1/documents?page=0&size=20
X-Tenant-Id: tenant-k
```

The response includes:

- document ID
- title
- filename
- category
- status
- size
- chunk count
- uploaded timestamp

---

## Get document

```http
GET /api/v1/documents/{id}
X-Tenant-Id: tenant-k
```

---

## Delete document

```http
DELETE /api/v1/documents/{id}
X-Tenant-Id: tenant-k
```

Deleting a document removes its associated chunks and embeddings through the
database foreign-key cascade.

Deleted documents therefore stop being available to retrieval immediately.

---

# Chat API

## Start a conversation

```http
POST /api/v1/chat
X-Tenant-Id: tenant-k
Content-Type: application/json
```

Request:

```json
{
  "question": "What is the tuition fee for undergraduate students?",
  "category": "Policy"
}
```

The server creates a new conversation when `conversationId` is not provided.

---

## Continue a conversation

```json
{
  "conversationId": "conversation-uuid",
  "question": "What about postgraduate students?",
  "category": "Policy"
}
```

The conversation history is loaded and supplied to the prompt so that
follow-up questions can resolve references to earlier messages.

Example:

```text
User:
What is the tuition fee for undergraduate students?

Assistant:
The standard tuition fee for undergraduate students is ₹60,000 per semester.

User:
What about postgraduate students?

Assistant:
For postgraduate students, the standard tuition fee is ₹75,000 per semester.
```

The factual answer still comes from retrieved document context.

---

## Chat response

```json
{
  "conversationId": "conversation-uuid",
  "answer": "The standard tuition fee for undergraduate students is ₹60,000 per semester.",
  "sources": [
    {
      "documentTitle": "Student Fee Policy",
      "pageNumber": 1,
      "similarity": 0.64,
      "snippet": "The standard tuition fee for undergraduate students..."
    }
  ]
}
```

---

# Conversation API

## Get conversation

```http
GET /api/v1/conversations/{id}
X-Tenant-Id: tenant-k
```

Returns the complete conversation history.

Example:

```json
{
  "conversationId": "conversation-uuid",
  "title": "What is the tuition fee for undergraduate students?",
  "createdAt": "...",
  "lastMessageAt": "...",
  "messages": [
    {
      "role": "USER",
      "content": "What is the tuition fee for undergraduate students?",
      "createdAt": "..."
    },
    {
      "role": "ASSISTANT",
      "content": "The standard tuition fee for undergraduate students is ₹60,000 per semester.",
      "createdAt": "..."
    }
  ]
}
```

---

# Retrieval Strategy

The application uses PostgreSQL + pgvector for vector similarity search.

Each document chunk stores a 1536-dimensional embedding.

The database uses an HNSW index with cosine distance:

```sql
CREATE INDEX idx_document_chunks_embedding_hnsw
ON document_chunks
USING hnsw (embedding vector_cosine_ops);
```

This allows vector search to scale better than a full sequential scan as the
number of chunks grows.

---

## Top-K

The default number of retrieved chunks is:

```text
5
```

Configured through:

```yaml
retrieval:
  top-k: 5
```

This keeps the prompt context bounded while providing multiple relevant
pieces of evidence.

---

## Similarity Threshold

The current similarity threshold is:

```text
0.5
```

Configured through:

```yaml
retrieval:
  similarity-threshold: 0.5
```

Chunks below this threshold are excluded from the prompt.

If no chunks remain after filtering, the application does not call the LLM
and returns:

```text
I could not find relevant information in the provided documents.
```

The threshold is intentionally kept configurable so it can be tuned against
a retrieval evaluation set rather than being hard-coded permanently.

---

# Chunking Strategy

The current chunking implementation uses:

```text
Chunk size: 800 tokens
Overlap:    120 tokens
```

Because the implementation uses a lightweight token approximation:

```text
1 token ≈ 4 characters
```

the corresponding character values are:

```text
Chunk size:  3200 characters
Overlap:      480 characters
```

The chunker preserves page information from the extraction stage so that
retrieved results can be traced back to the source document page.

### Why overlap?

The 120-token overlap helps preserve context across chunk boundaries.

For example, if a sentence or related information spans two chunks, the
overlap increases the chance that both pieces remain available to retrieval.

### Why 800 tokens?

The goal is to balance:

- enough context per chunk
- useful retrieval granularity
- manageable embedding/input size
- reasonable prompt size

The chunk size is configurable at implementation level and can be tuned
using a retrieval evaluation dataset.

---

# Embeddings

The default embedding configuration is:

```text
Provider:   Google Gemini
Model:      gemini-embedding-001
Dimensions: 1536
```

The embedding model is used for both:

1. Document chunk embeddings during ingestion.
2. Query embeddings during retrieval.

This ensures that document and query vectors exist in the same embedding
space.

Embeddings are generated in batches rather than making one API call for every
individual chunk.

### Cost

The exact embedding cost depends on the provider pricing and the amount of
text processed.

For this assignment, embedding cost is primarily driven by the total token
volume sent during document ingestion rather than the number of database
rows.

---

# Grounding and Refusal

The application uses a strict document-grounded prompt.

The model is instructed to:

- answer only from retrieved document context
- avoid unsupported claims
- preserve dates, amounts, names and rules
- avoid adding external knowledge
- refuse when the context does not contain enough information

The retrieval layer also has a similarity threshold.

Therefore the refusal flow is:

```text
Question
   ↓
Vector retrieval
   ↓
Similarity filtering
   ↓
No qualifying chunks
   ↓
Fixed refusal response
   ↓
No LLM call
```

This reduces the chance of generating plausible but unsupported answers.

---

# Conversation Memory

Conversation state is persisted in PostgreSQL.

The following configuration controls the amount of previous history supplied
to the model:

```yaml
conversation:
  max-history-turns: 6
  max-history-tokens: 3000
```

Both limits are applied.

### Turn limit

At most six previous conversational turns are included.

### Token budget

History is also limited to approximately 3000 estimated tokens.

Messages store their estimated token count in the database.

The current token estimator uses:

```text
estimated tokens ≈ character count / 4
```

This keeps the implementation independent from a provider-specific tokenizer.

A model-specific tokenizer could replace this approximation in a future
version.

---

# Source Provenance

Every assistant response stores the retrieved source chunks in
`message_sources`.

The relationship is:

```text
messages
   │
   │ message_id
   ▼
message_sources
   │
   │ chunk_id
   ▼
document_chunks
```

Each source stores:

- assistant message ID
- document chunk ID
- similarity score

The API also returns source references containing the document title,
page number, similarity score and snippet.

This provides both response-time citations and persistent provenance.

---

# Multi-Tenancy

The application uses the `X-Tenant-Id` header:

```http
X-Tenant-Id: tenant-k
```

Tenant filtering is applied to document and retrieval operations.

Conversations also verify that the requested conversation belongs to the
provided tenant.

This means a conversation or document belonging to one tenant cannot be
retrieved by another tenant simply by knowing its UUID.

Tenant identity is currently supplied through the request header.

Authentication and tenant authorization are intentionally left for a later
security layer.

---

# Idempotent Ingestion

During upload, the application calculates a SHA-256 hash of the file.

The database enforces:

```sql
UNIQUE (tenant_id, content_hash)
```

This prevents the same document from being uploaded multiple times for the
same tenant.

The same file can still exist for different tenants because the tenant ID is
part of the uniqueness constraint.

---

# Document Lifecycle

Documents move through the following states:

```text
PROCESSING
     │
     ├── successful ingestion ──→ READY
     │
     └── ingestion failure ─────→ FAILED
```

The document status is stored in PostgreSQL.

This allows the upload API to return immediately while ingestion continues
asynchronously.

---

# Storage

The current implementation stores uploaded files on the local filesystem.

Default:

```yaml
storage:
  path: ./uploads
```

This was chosen to keep the assignment simple and easy to run locally.

For production deployment, object storage such as Amazon S3 would be a better
choice.

A production version could use presigned uploads and keep the application
containers stateless.

---

# Database

Flyway manages database migrations.

The initial schema creates:

```text
documents
document_chunks
conversations
messages
message_sources
```

The main relationships are:

```text
documents
    │
    └── document_chunks

conversations
    │
    └── messages
            │
            └── message_sources
                    │
                    └── document_chunks
```

Document chunks use a foreign key with `ON DELETE CASCADE`, so deleting a
document also removes its chunks and embeddings.

---

# Error Handling

The application uses a global exception handler.

Examples:

| Error                     | HTTP status |
| ------------------------- | ----------: |
| Invalid request           |         400 |
| Duplicate document        |         409 |
| Unsupported document type |         415 |
| File too large            |         413 |

Blank or missing `X-Tenant-Id` values are also rejected.

---

# Design Decisions and Trade-offs

## PostgreSQL + pgvector instead of a separate vector database

Using pgvector keeps relational metadata and vector data in the same
database.

This simplifies:

- tenant filtering
- document/chunk relationships
- deletion
- transactions
- local development

For the expected assignment scale, a separate vector database would add
operational complexity without a clear benefit.

---

## Local filesystem instead of S3

Local storage makes the project easier to run and test.

For production, S3 or another object storage service would be preferable for:

- durability
- horizontal scaling
- stateless application containers
- large file handling

---

## Approximate token counting

The application uses a simple character-based token estimate instead of
coupling the conversation layer to a provider-specific tokenizer.

This is sufficient for enforcing a bounded history budget, but it is not
identical to the tokenizer used by every LLM.

A provider/model-specific tokenizer would be more accurate in production.

---

# Limitations

Current limitations include:

- Local filesystem document storage.
- Tenant identity is supplied through `X-Tenant-Id` and full authentication/
  authorization is not yet implemented.
- Token counting is approximate.
- Retrieval currently uses vector similarity rather than hybrid lexical +
  semantic search.
- The application currently exposes non-streaming chat.
- Retrieval quality depends on the selected embedding model, chunking
  strategy and similarity threshold.
- A dedicated retrieval evaluation dataset would improve threshold and
  chunking calibration.

---

# What I Would Improve With Two More Weeks

If additional development time were available, I would prioritize:

1. Amazon S3 based document storage.
2. Authentication and tenant authorization.
3. Streaming chat responses.
4. Model-specific token counting.
5. Retrieval evaluation with a golden dataset.
6. Hybrid BM25/full-text + vector retrieval.
7. Reranking of retrieved chunks.
8. Better observability for retrieval quality, latency and model cost.
9. Retry/backoff handling for external AI providers.
10. Production deployment with proper secrets and infrastructure management.

---

# Testing

The project contains automated tests for the implemented functionality.

Important scenarios include:

- Document upload
- File validation
- Duplicate detection
- Document listing
- Document detail
- Document deletion
- Conversation creation
- Conversation history
- Follow-up questions
- Token-budgeted history
- Tenant-scoped access
- Retrieval refusal
- Source persistence

Run all tests with:

```bash
./mvnw clean test
```

On Windows:

```powershell
.\mvnw.cmd clean test
```

---

# Health Check

Spring Boot Actuator exposes:

```http
GET /actuator/health
```

Example:

```json
{
  "status": "UP"
}
```

---

# Assignment Checklist

| Requirement                   | Status |
| ----------------------------- | ------ |
| Document upload               | ✅     |
| Asynchronous ingestion        | ✅     |
| PDF/DOCX/TXT/Markdown support | ✅     |
| SHA-256 idempotency           | ✅     |
| Batched embeddings            | ✅     |
| PostgreSQL + pgvector         | ✅     |
| HNSW vector index             | ✅     |
| Document list API             | ✅     |
| Document detail API           | ✅     |
| Document deletion API         | ✅     |
| Grounded chat                 | ✅     |
| Source citations              | ✅     |
| Refusal path                  | ✅     |
| Conversation persistence      | ✅     |
| Conversation memory           | ✅     |
| Token-budgeted history        | ✅     |
| Conversation history API      | ✅     |
| Tenant-scoped retrieval       | ✅     |
| Message source persistence    | ✅     |
| Flyway migrations             | ✅     |
| Automated tests               | ✅     |
| Streaming chat                | ⏳     |
| Production object storage     | ⏳     |
| Authentication/authorization  | ⏳     |

---

# One Observation

One of the most important observations during development was that a fluent
LLM response does not necessarily mean retrieval was good.

A RAG system therefore needs a strong refusal path in addition to generation.

The application handles this by applying a similarity threshold before
building the prompt. If no retrieved chunk is sufficiently relevant, the
application returns a fixed refusal instead of asking the LLM to guess.

This makes retrieval quality a first-class part of the system rather than
treating the LLM as the only component responsible for answer quality.
