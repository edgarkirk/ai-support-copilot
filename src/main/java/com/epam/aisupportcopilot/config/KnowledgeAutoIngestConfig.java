package com.epam.aisupportcopilot.config;

import com.epam.aisupportcopilot.repository.KnowledgeChunkRepository;
import com.epam.aisupportcopilot.service.KnowledgeIngestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class KnowledgeAutoIngestConfig {

    private final KnowledgeChunkRepository knowledgeChunkRepository;
    private final KnowledgeIngestion knowledgeIngestion;

    @EventListener(ApplicationReadyEvent.class)
    public void ingestOnStartupIfEmpty() {
        if (knowledgeChunkRepository.count() > 0) {
            log.info("Knowledge base already populated, skipping auto-ingestion");
            return;
        }

        log.info("Knowledge base is empty, starting auto-ingestion...");
        try {
            int chunks = knowledgeIngestion.ingestAllDocuments();
            log.info("Auto-ingestion complete: {} chunks ingested", chunks);
        } catch (Exception e) {
            log.error("Auto-ingestion failed", e);
        }
    }
}