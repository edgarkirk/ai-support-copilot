package com.epam.aisupportcopilot.service;

import com.epam.aisupportcopilot.entity.KnowledgeChunk;
import com.epam.aisupportcopilot.repository.KnowledgeChunkRepository;
import com.epam.aisupportcopilot.util.VectorUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeIngestionService implements KnowledgeIngestion {

    private final KnowledgeChunkRepository knowledgeChunkRepository;
    private final EmbeddingModel embeddingModel;
    private final ChunkingStrategy chunkingStrategy;

    @Override
    @Transactional
    public int ingestAllDocuments() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:data/knowledge/**/*.md");

        int totalChunks = 0;

        for (Resource resource : resources) {
            String filename = resource.getFilename();
            log.info("Ingesting document: {}", filename);

            String content = resource.getContentAsString(StandardCharsets.UTF_8);
            List<String> chunks = chunkingStrategy.chunk(content);

            knowledgeChunkRepository.deleteAllBySource(filename);

            for (String chunk : chunks) {
                String trimmedContent = chunk.trim();
                KnowledgeChunk entity = KnowledgeChunk.builder()
                        .source(filename)
                        .content(trimmedContent)
                        .build();
                entity = knowledgeChunkRepository.save(entity);

                float[] vector = embeddingModel.embed(trimmedContent);
                knowledgeChunkRepository.updateEmbedding(entity.getId(), VectorUtils.toVectorString(vector));

                totalChunks++;
            }

            log.info("  -> {} chunks from {}", chunks.size(), filename);
        }

        log.info("Ingestion complete: {} total chunks", totalChunks);
        return totalChunks;
    }
}