package com.epam.aisupportcopilot.service;

import com.epam.aisupportcopilot.dto.KnowledgeChunkResult;
import java.util.List;

public interface KnowledgeRetrieval {

    List<KnowledgeChunkResult> findRelevant(String query, int topK);
}