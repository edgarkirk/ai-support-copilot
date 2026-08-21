package com.epam.aisupportcopilot.service.knowledge;

import java.io.IOException;

/**
 * Ingests markdown knowledge documents into the vector store for RAG retrieval.
 */
public interface KnowledgeIngestion {

    /**
     * Scans all markdown files under {@code classpath:data/knowledge/}, chunks them,
     * generates embeddings, and stores them in the database.
     *
     * @return total number of chunks ingested across all documents
     * @throws IOException if documents cannot be read from the classpath
     */
    int ingestAllDocuments() throws IOException;
}