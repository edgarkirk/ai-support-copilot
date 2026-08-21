package com.epam.aisupportcopilot.tools;

import com.epam.aisupportcopilot.dto.KnowledgeChunkResult;
import com.epam.aisupportcopilot.service.knowledge.KnowledgeRetrieval;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * LLM-callable tool for searching the RAG knowledge base.
 * Delegates to {@link KnowledgeRetrieval} which performs hybrid vector + keyword search
 * with Reciprocal Rank Fusion scoring.
 */
@Component
@RequiredArgsConstructor
public class KnowledgeTools {

    private static final int DEFAULT_RETRIEVAL_LIMIT = 5;

    private final KnowledgeRetrieval knowledgeRetrievalService;

    @Tool(description = "Search the support knowledge base for relevant information. " +
        "Searches troubleshooting guides, policies, incident postmortems, and known issues. " +
        "Use this when you need procedural guidance, policy details, or incident history. " +
        "You can call this tool multiple times with different queries to find more relevant information. " +
        "Use specific search terms related to the topic (e.g. 'authentication troubleshooting', " +
        "'authentication incident postmortem', 'stale session HTTP 401').")
    public String searchKnowledge(
        @ToolParam(description = "Search query — use specific keywords related to the topic") String query) {
        List<KnowledgeChunkResult> chunks = knowledgeRetrievalService.findRelevant(query, DEFAULT_RETRIEVAL_LIMIT);

        if (chunks.isEmpty()) {
            return "No relevant knowledge found for: " + query;
        }

        return chunks.stream()
            .map(c -> "--- Source: " + c.source() + " ---\n" + c.content())
            .collect(Collectors.joining("\n\n"));
    }
}