# AI Support Copilot

AI Support Copilot is a proof-of-concept Spring Boot application that
explores how a Large Language Model can combine **structured
support-ticket data** with **unstructured support knowledge** to answer
operational questions in natural language.

The main goal is to learn and demonstrate a practical enterprise AI
workflow without overengineering:

**PostgreSQL + RAG + LLM**

## Problem

Support information is usually distributed across multiple sources:

-   ticket and incident databases;
-   troubleshooting guides;
-   support policies;
-   incident post-mortems;
-   operational documentation.

Answering a question such as:

> Why did authentication issues increase recently, and what should the
> support team do?

may require both quantitative analysis of ticket data and searching
internal documentation.

AI Support Copilot aims to combine those sources into one grounded
answer.

## Target Scenarios

### Structured data question

> How many authentication issues occurred in July?

The application retrieves facts from PostgreSQL and uses them as context
for the AI response.

### Knowledge question

> What should support do when authentication failures increase?

The application retrieves relevant knowledge using RAG.

### Mixed question

> Authentication issues increased significantly in July. Based on our
> ticket data and support knowledge, what is the likely problem and what
> should we do?

This is the primary PoC scenario.

The intended flow is:

``` text
User question
      |
      +-------------------+
      |                   |
      v                   v
SQL ticket analysis    RAG search
      |                   |
      +---------+---------+
                |
                v
          Combined context
                |
                v
               LLM
                |
                v
          Grounded answer
```

## Technology Stack

-   Java
-   Spring Boot
-   Spring Web
-   Spring Data JPA
-   Maven
-   PostgreSQL
-   pgvector
-   Flyway
-   Docker Compose
-   LLM integration
-   Embeddings and Retrieval-Augmented Generation (RAG)

## Data

### Structured data

The PoC uses a synthetic dataset containing **10,000 support tickets**.

The PostgreSQL table is:

``` text
ticket

id
created_at
customer_id
product
category
priority
status
description
resolution
resolved_at
```

Example categories include:

-   AUTHENTICATION
-   PAYMENT
-   REFUND
-   ACCOUNT
-   PERFORMANCE
-   UI
-   NOTIFICATIONS
-   SUBSCRIPTION
-   DELIVERY
-   SEARCH
-   PROFILE

The dataset contains deliberately injected patterns to make the demo
reproducible.

For example, authentication tickets increase significantly in July and
August, while payment tickets contain a concentrated spike near the end
of July.

Structured data is stored under:

``` text
src/main/resources/data/structured/
```

### Unstructured data

A small support knowledge base will be used for RAG.

Planned content includes:

-   authentication troubleshooting;
-   payment troubleshooting;
-   support policies;
-   incident post-mortems.

Markdown is used initially to keep document ingestion simple and focus
on learning RAG rather than document parsing.

## Architecture

``` text
                    User
                      |
                      v
              Spring Boot REST API
                      |
                      v
              Question processing
                /            \
               /              \
              v                v
     Structured data          RAG
       PostgreSQL          Knowledge docs
          |                    |
          |                Embeddings
          |                    |
          |                 pgvector
          \                    /
           \                  /
            v                v
             Context Builder
                   |
                   v
                  LLM
                   |
                   v
             Grounded Answer
```

PostgreSQL is used for both relational ticket data and vector storage
through pgvector, avoiding the need for a separate vector database in
the PoC.

## Running PostgreSQL

Start the database:

``` bash
docker compose up -d
```

The PostgreSQL container is exposed on:

``` text
localhost:5433
```

Default development database configuration:

``` text
Database: ai_support
Username: ai_support
Password: ai_support
```

The Spring Boot datasource therefore uses:

``` text
jdbc:postgresql://localhost:5433/ai_support
```

## Database Schema

Flyway migrations are located under:

``` text
src/main/resources/db/migration/
```

The initial migration creates the `ticket` table and enables pgvector:

``` sql
CREATE EXTENSION IF NOT EXISTS vector;
```

Flyway applies migrations automatically when the Spring Boot application
starts.

## Importing the Ticket Dataset

The structured dataset and import script are located under:

``` text
src/main/resources/data/structured/
├── tickets.csv
└── import_tickets.sql
```

If `psql` is not installed locally, the PostgreSQL client inside the
Docker container can be used.

From the structured-data directory, copy the files into the container:

``` bash
docker cp tickets.csv ai-support-postgres:/tmp/tickets.csv
docker cp import_tickets.sql ai-support-postgres:/tmp/import_tickets.sql
```

Run the import:

``` bash
docker exec -it ai-support-postgres \
  psql -U ai_support -d ai_support \
  -f /tmp/import_tickets.sql
```

Verify the number of records:

``` bash
docker exec -it ai-support-postgres \
  psql -U ai_support -d ai_support \
  -c "SELECT COUNT(*) FROM ticket;"
```

Expected result:

``` text
10000
```

## Running the Application

Run tests:

``` bash
./mvnw test
```

Start Spring Boot:

``` bash
./mvnw spring-boot:run
```

The application is available by default at:

``` text
http://localhost:8080
```

## AI Design

The PoC will use AI at runtime.

The intended request flow is:

``` text
User question
      |
      v
Spring Boot
      |
      +----> query structured ticket data
      |
      +----> retrieve relevant knowledge with RAG
      |
      v
Build grounded context
      |
      v
Send request to LLM
      |
      v
Return generated answer
```

The LLM should not be treated as the source of ticket facts. Facts
should come from PostgreSQL, and support knowledge should come from
retrieved documents.

The LLM's main role is to interpret the question and synthesize the
supplied evidence into a useful response.

## PoC Scope

The project intentionally avoids unnecessary production complexity.

Out of scope for the initial PoC:

-   production authentication and authorization;
-   complex frontend development;
-   multiple databases;
-   a separate vector database;
-   large-scale document ingestion;
-   autonomous agents;
-   advanced orchestration frameworks;
-   production monitoring;
-   generic CRUD functionality.

The focus is on understanding:

-   real-time LLM integration;
-   structured-data grounding;
-   embeddings;
-   vector similarity search;
-   RAG;
-   combining structured and unstructured context;
-   grounded AI responses.

## Demo Goal

The final demonstration should progress through three questions:

**1. Structured data**

> How many authentication issues occurred in July?

**2. RAG**

> What should support do when authentication failures increase?

**3. SQL + RAG + LLM**

> Why did authentication issues increase recently, and what should we
> do?

The third question is the main demonstration of the project.

## Project Goal

The project is successful when a user can ask a natural-language support
question and receive a real-time AI-generated response grounded in both:

1.  actual support-ticket statistics from PostgreSQL; and
2.  relevant information retrieved from the support knowledge base.
