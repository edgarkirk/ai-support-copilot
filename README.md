# AI Support Copilot

AI Support Copilot is a proof-of-concept Spring Boot application that
demonstrates how a Large Language Model can combine **structured
support-ticket data** with **unstructured support knowledge** to answer
operational questions in natural language.

**PostgreSQL + RAG + LLM + Tool Calling + Streaming**

## Problem

Support information is usually distributed across multiple sources:

- ticket and incident databases;
- troubleshooting guides;
- support policies;
- incident post-mortems;
- operational documentation.

Answering a question such as:

> Why did authentication issues increase recently, and what should the
> support team do?

requires both quantitative analysis of ticket data and searching
internal documentation. AI Support Copilot combines those sources into
one grounded answer.

## Target Scenarios

### Structured data question

> How many authentication issues occurred in July?

The LLM calls a **ticket data tool** to query PostgreSQL, then uses the
result to form the answer.

### Knowledge question

> What should support do when authentication failures increase?

The LLM calls a **knowledge search tool** to retrieve relevant documents
via hybrid RAG (vector + full-text search).

### Mixed question

> Authentication issues increased significantly in July. Based on our
> ticket data and support knowledge, what is the likely problem and what
> should we do?

The LLM calls **both tools**, combining ticket statistics with knowledge
base content to produce a grounded answer with source citations.

## Architecture

```
                        User
                         |
                         v
                    Chat UI (SSE)
                         |
                         v
                  Spring Boot API
                         |
                         v
                   ChatClient + Tools
                    /              \
                   /                \
                  v                  v
         TicketTools (SQL)    KnowledgeTools (RAG)
              |                      |
              v                      v
          PostgreSQL            pgvector
        (ticket data)     (hybrid search: vector
                           + full-text with RRF)
                  \                  /
                   \                /
                    v              v
                     LLM (GPT-4o)
                         |
                         v
                  Streamed answer
                  with source citations
```

### Key Design Decisions

- **Tool calling** — the LLM decides which tools to invoke. No hardcoded
  routing or context stuffing.
- **Hybrid RAG** — vector cosine similarity + PostgreSQL full-text search,
  merged via Reciprocal Rank Fusion (RRF).
- **Single database** — PostgreSQL serves both relational data and vector
  storage (pgvector), avoiding a separate vector database.
- **SSE streaming** — tokens stream to the UI in real-time via
  Server-Sent Events using `SseEmitter`.
- **Observable tool callbacks** — decorator pattern wraps tool execution
  to emit real-time tool call indicators to the UI.

## Technology Stack

| Layer      | Technology                                |
|------------|-------------------------------------------|
| Language   | Java 21                                   |
| Framework  | Spring Boot 4.1, Spring AI 1.1.2          |
| AI Model   | Azure OpenAI (GPT-4o) via EPAM DIAL proxy |
| Embeddings | text-embedding-3-small                    |
| Database   | PostgreSQL 17 + pgvector                  |
| Migrations | Flyway                                    |
| Build      | Maven                                     |
| Container  | Docker Compose                            |
| UI         | Thymeleaf + vanilla JS                    |

## AI Techniques Demonstrated

| Technique                                | Implementation                                                                       |
|------------------------------------------|--------------------------------------------------------------------------------------|
| **LLM Tool Calling**                     | Spring AI `@Tool` annotations; LLM autonomously selects which tools to call          |
| **RAG (Retrieval-Augmented Generation)** | Knowledge base documents chunked, embedded, and stored in pgvector                   |
| **Hybrid Search**                        | Vector similarity + full-text search combined via RRF                                |
| **Chunking Strategy**                    | Strategy pattern — section-based splitting by markdown headings                      |
| **Streaming**                            | SSE via `SseEmitter` with `Flux` from Spring AI's streaming API                      |
| **Tool Call Transparency**               | `ObservableToolCallback` decorator emits tool names as SSE events                    |
| **Guardrails**                           | System prompt constrains the LLM to support-related topics only                      |
| **Source Citations**                     | System prompt instructs LLM to cite document names and data queries                  |
| **Conversation Memory**                  | `MessageWindowChatMemory` maintains 20-message sliding window per session            |
| **Auto-ingestion**                       | Knowledge base documents are automatically ingested on startup if the table is empty |
| **RAG Evaluation**                       | Endpoint runs 10 test cases and scores answers by keyword matching                   |

## Data

### Structured data

A synthetic dataset of **10,000 support tickets** stored in PostgreSQL.

```
ticket(id, created_at, customer_id, product, category, priority, status,
       description, resolution, resolved_at)
```

Categories: AUTHENTICATION, PAYMENT, REFUND, ACCOUNT, PERFORMANCE, UI,
NOTIFICATIONS, SUBSCRIPTION, DELIVERY, SEARCH, PROFILE.

The dataset contains deliberately injected patterns — authentication
tickets spike in July/August, payment tickets spike late July.

### Unstructured data (Knowledge Base)

15 markdown documents across 5 categories:

```
src/main/resources/data/knowledge/
├── known-issues/          # Active bugs and workarounds
├── policies/              # Refund, escalation, SLA response times
├── postmortems/           # Incident reports with root cause analysis
├── product/               # Service architecture overviews
└── troubleshooting/       # Step-by-step troubleshooting guides
```

Covers three domains: **authentication**, **payments**, and
**notifications/accounts**.

## Database Schema

Three Flyway migrations:

| Migration                           | Description                                                        |
|-------------------------------------|--------------------------------------------------------------------|
| `V1__initial_schema.sql`            | `ticket` table + pgvector extension                                |
| `V2__knowledge_embeddings.sql`      | `knowledge_chunk` table with `vector(1536)` column + IVFFlat index |
| `V3__knowledge_fulltext_search.sql` | `content_tsv` generated column + GIN index for full-text search    |

## API Endpoints

| Endpoint                | Method | Description                                     |
|-------------------------|--------|-------------------------------------------------|
| `/`                     | GET    | Chat UI                                         |
| `/api/ai/ask`           | POST   | Blocking AI response                            |
| `/api/ai/ask/stream`    | POST   | SSE streaming AI response with tool call events |
| `/api/knowledge/ingest` | POST   | Re-ingest knowledge base documents              |
| `/api/evaluation/run`   | POST   | Run RAG evaluation suite (10 test cases)        |
| `/api/tickets/**`       | GET    | Ticket data endpoints for manual verification   |

## Getting Started

### Prerequisites

- Java 21
- Maven
- Docker

### 1. Start PostgreSQL

```bash
docker compose up -d
```

Exposed on `localhost:5433` (database: `ai_support`, user: `ai_support`,
password: `ai_support`).

### 2. Configure AI credentials

Set environment variables:

```bash
export AZURE_OPEN_AI_ENDPOINT=https://ai-proxy.lab.epam.com
export AZURE_OPEN_AI_KEY=your-api-key
```

To use a different model, override the deployment name:

```bash
export AZURE_OPEN_AI_DEPLOYMENT_NAME=gpt-4o
```

> **Note:** Non-OpenAI models (e.g. `anthropic.claude-sonnet-5`) behind the
> DIAL proxy may not support streaming with tool calls due to Spring AI's
> Azure OpenAI adapter expecting OpenAI-specific streaming chunk formats.
> The blocking endpoint (`/api/ai/ask`) works with any model.

### 3. Run the application

```bash
./mvnw spring-boot:run
```

The application starts at `http://localhost:8080`. On first startup,
Flyway creates the database schema and knowledge base documents are
**automatically ingested**.

### 4. Import ticket data

In a separate terminal (the app must be running so the `ticket` table exists):

```bash
docker cp src/main/resources/data/structured/tickets.csv ai-support-postgres:/tmp/tickets.csv
docker cp src/main/resources/data/structured/import_tickets.sql ai-support-postgres:/tmp/import_tickets.sql
docker exec ai-support-postgres \
  psql -U ai_support -d ai_support -f /tmp/import_tickets.sql
```

### 5. Verify setup

Verify ticket data is loaded:

```bash
curl -s http://localhost:8080/api/tickets/count/AUTHENTICATION
```

Verify knowledge base is populated:

```bash
curl -s -X POST http://localhost:8080/api/ai/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "What is the refund policy?"}'
```

## Usage

### Chat UI

Open `http://localhost:8080` for the interactive chat interface. Features:

- Real-time **streaming** responses (tokens appear as they are generated)
- **Tool call indicators** with spinners showing which tools the LLM is
  calling (e.g., "Searching knowledge base", "Querying ticket count")
- **Conversation memory** — follow-up questions work within the same session
- Example questions to get started

### API (curl examples)

**Blocking response:**

```bash
curl -s -X POST http://localhost:8080/api/ai/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "How many authentication issues occurred in July?"}'
```

**Streaming response (SSE):**

```bash
curl -s -N -X POST http://localhost:8080/api/ai/ask/stream \
  -H "Content-Type: application/json" \
  -d '{"question": "How many authentication issues occurred in July?"}'
```

The SSE stream emits named events:

- `tool_call` — name of the tool being executed
- `content` — text token from the LLM response
- `done` — stream complete
- `error` — error occurred

**RAG evaluation (runs 10 test cases, takes 1-2 minutes):**

```bash
curl -s -X POST http://localhost:8080/api/evaluation/run | python3 -m json.tool
```

Returns a JSON report with pass/fail per test case, matched/missed
keywords, and an overall score.

## Demo Questions

### Structured data (SQL tools)

```bash
curl -s -X POST http://localhost:8080/api/ai/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "How many authentication issues occurred in July?"}'
```

### Knowledge base (RAG)

```bash
curl -s -X POST http://localhost:8080/api/ai/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "What should support do when authentication failures increase?"}'
```

### Mixed — SQL + RAG + LLM (primary scenario)

```bash
curl -s -X POST http://localhost:8080/api/ai/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "Authentication issues increased significantly in July. Based on our ticket data and support knowledge, what is the likely problem and what should we do?"}'
```

### Guardrails (off-topic rejection)

```bash
curl -s -X POST http://localhost:8080/api/ai/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "How do I make pasta?"}'
```

### Notifications domain

```bash
curl -s -X POST http://localhost:8080/api/ai/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "A customer is not receiving order confirmation emails. What should I do?"}'
```

### Account domain

```bash
curl -s -X POST http://localhost:8080/api/ai/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "A customer account is locked after too many failed login attempts. How do I help them?"}'
```

### SLA policy

```bash
curl -s -X POST http://localhost:8080/api/ai/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "What is the SLA response time for critical incidents?"}'
```

## Project Scope

This is a learning PoC for an AI course. It intentionally avoids
production complexity (auth, complex frontend, multiple databases,
agents, monitoring) and focuses on demonstrating AI integration patterns:

- LLM tool calling (not context stuffing)
- Hybrid RAG with RRF (not naive top-K vector search)
- Streaming with tool call transparency
- Grounded responses with source citations
- Evaluation of RAG quality