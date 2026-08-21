# AI Support Copilot — Concept

Spring Boot PoC that combines structured ticket data (PostgreSQL) with unstructured support knowledge (RAG) via LLM tool calling to answer support questions in natural language.

**Problem:** Support info is spread across ticket databases, troubleshooting guides, policies, and postmortems. Answering "Why did auth issues spike in July?" requires querying data AND searching docs manually.

**Solution:** LLM orchestrates both — calls ticket data tools for trends and knowledge search tools for documentation, then synthesizes a grounded answer with source citations.

## Architecture

User -> Chat UI (SSE) -> Spring Boot API -> ChatClient + Tools -> TicketTools (PostgreSQL) + KnowledgeTools (pgvector hybrid RAG) -> LLM (GPT-4o) -> Streamed answer with citations

## Tech Stack

Java 21, Spring Boot 4.1, Spring AI 1.1.2, Azure OpenAI (GPT-4o) via EPAM DIAL, PostgreSQL 17 + pgvector, Flyway, Maven, Docker Compose, Thymeleaf + vanilla JS

## AI Techniques (11)

1. **LLM Tool Calling** — Spring AI @Tool; LLM autonomously selects tools
2. **RAG** — documents chunked, embedded, stored in pgvector
3. **Hybrid Search** — vector + full-text search via Reciprocal Rank Fusion
4. **Chunking Strategy** — Strategy pattern, section-based splitting by markdown headings
5. **SSE Streaming** — SseEmitter + Flux, real-time token streaming
6. **Tool Call Transparency** — ObservableToolCallback decorator emits tool names to UI
7. **Guardrails** — system prompt constrains to support topics only
8. **Source Citations** — LLM cites document names and data queries
9. **Conversation Memory** — 20-message sliding window per session
10. **Auto-ingestion** — knowledge base ingested on startup if empty
11. **RAG Evaluation** — 10 test cases, keyword matching, pass/fail scoring

## Data

- **Structured:** 10,000 synthetic tickets across 11 categories with injected patterns (auth spike in July)
- **Unstructured:** 15 markdown docs (known issues, policies, postmortems, product docs, troubleshooting) across 3 domains

## Design Patterns

Strategy (chunking), Decorator (observable tools), Sealed Interface (stream events), Interface Segregation (service boundaries)

## Scope

Learning PoC for AI course. Intentionally excludes auth, cloud deployment, complex frontend, monitoring. Focuses on AI integration patterns: tool calling, hybrid RAG, streaming, guardrails, evaluation.