package com.epam.aisupportcopilot.service.knowledge;

import com.epam.aisupportcopilot.dto.KnowledgeChunkResult;
import com.epam.aisupportcopilot.repository.KnowledgeChunkRepository;
import com.epam.aisupportcopilot.util.VectorUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Performs hybrid search combining pgvector cosine similarity and PostgreSQL full-text search,
 * merged via Reciprocal Rank Fusion (RRF). Results below a relevance threshold are filtered out.
 * Query embeddings are cached via {@code @Cacheable} to avoid redundant embedding API calls.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeRetrievalService implements KnowledgeRetrieval {

    private static final int CANDIDATE_MULTIPLIER = 3;

    private final KnowledgeChunkRepository knowledgeChunkRepository;
    private final EmbeddingModel embeddingModel;

    @Override
    public List<KnowledgeChunkResult> findRelevant(String query, int topK) {
        float[] queryVector = embedQuery(query);
        return knowledgeChunkRepository
                .findHybridWithScore(VectorUtils.toVectorString(queryVector), query, topK * CANDIDATE_MULTIPLIER)
                .stream()
                .filter(row -> ((Number) row[3]).doubleValue() > 0.02)
                .limit(topK)
                .map(row -> new KnowledgeChunkResult((String) row[1], (String) row[2]))
                .toList();
    }

    @Cacheable(value = "embeddings", key = "#query")
    public float[] embedQuery(String query) {
        return embeddingModel.embed(query);
    }
}