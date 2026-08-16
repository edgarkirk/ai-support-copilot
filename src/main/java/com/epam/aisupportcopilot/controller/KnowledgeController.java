package com.epam.aisupportcopilot.controller;

import com.epam.aisupportcopilot.service.KnowledgeIngestion;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Slf4j
public class KnowledgeController {

    private final KnowledgeIngestion knowledgeIngestion;

    @PostMapping("/ingest")
    public String ingest() {
        try {
            int chunks = knowledgeIngestion.ingestAllDocuments();
            return "Ingested " + chunks + " chunks";
        } catch (IOException ex) {
            log.error("Knowledge ingestion failed", ex);
            throw new IllegalStateException("Knowledge ingestion failed: " + ex.getMessage());
        }
    }
}