package com.epam.aisupportcopilot.service.knowledge;

import com.epam.aisupportcopilot.dto.KnowledgeChunkResult;
import java.util.List;

/**
 * Retrieves relevant knowledge chunks using hybrid search (vector similarity + keyword matching).
 */
public interface KnowledgeRetrieval {

    /**
     * Finds the most relevant knowledge chunks for a given query using Reciprocal Rank Fusion
     * of vector cosine similarity and PostgreSQL full-text search scores.
     *
     * @param query natural-language search query
     * @param topK  maximum number of results to return
     * @return ranked list of matching chunks with source metadata
     */
    List<KnowledgeChunkResult> findRelevant(String query, int topK);
}