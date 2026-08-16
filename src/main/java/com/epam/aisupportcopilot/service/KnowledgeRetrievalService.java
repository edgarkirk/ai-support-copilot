package com.epam.aisupportcopilot.service;

import com.epam.aisupportcopilot.dto.KnowledgeChunkResult;
import com.epam.aisupportcopilot.entity.KnowledgeChunk;
import com.epam.aisupportcopilot.repository.KnowledgeChunkRepository;
import com.epam.aisupportcopilot.util.VectorUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeRetrievalService implements KnowledgeRetrieval {

    private final KnowledgeChunkRepository knowledgeChunkRepository;
    private final EmbeddingModel embeddingModel;

    @Override
    public List<KnowledgeChunkResult> findRelevant(String query, int topK) {
        float[] queryVector = embeddingModel.embed(query);
        return knowledgeChunkRepository
                .findHybrid(VectorUtils.toVectorString(queryVector), query, topK)
                .stream()
                .map(this::toResult)
                .toList();
    }

    private KnowledgeChunkResult toResult(KnowledgeChunk chunk) {
        return new KnowledgeChunkResult(chunk.getSource(), chunk.getContent());
    }
}