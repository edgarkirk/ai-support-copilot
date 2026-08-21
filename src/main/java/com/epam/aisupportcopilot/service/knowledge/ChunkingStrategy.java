package com.epam.aisupportcopilot.service.knowledge;

import java.util.List;

/**
 * Strategy for splitting document content into smaller chunks suitable for embedding and retrieval.
 */
public interface ChunkingStrategy {

    /**
     * Splits the given document content into a list of text chunks.
     *
     * @param content full document text (typically markdown)
     * @return ordered list of text chunks
     */
    List<String> chunk(String content);
}