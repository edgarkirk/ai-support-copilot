package com.epam.aisupportcopilot.service.knowledge;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Splits markdown documents at H1/H2 headings ({@code # } / {@code ## }) and adds a
 * {@value #OVERLAP_LINES}-line overlap from the previous section to preserve context
 * across chunk boundaries.
 */
@Component
public class SectionChunkingStrategy implements ChunkingStrategy {

    private static final int OVERLAP_LINES = 3;

    @Override
    public List<String> chunk(String content) {
        String[] sections = content.split("(?m)(?=^#{1,2} )");

        List<String> rawChunks = new ArrayList<>();
        for (String section : sections) {
            String trimmed = section.trim();
            if (!trimmed.isEmpty()) {
                rawChunks.add(trimmed);
            }
        }

        if (rawChunks.isEmpty()) {
            return List.of(content.trim());
        }

        if (rawChunks.size() == 1) {
            return List.copyOf(rawChunks);
        }

        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < rawChunks.size(); i++) {
            if (i == 0) {
                chunks.add(rawChunks.get(i));
            } else {
                String overlap = getTrailingLines(rawChunks.get(i - 1), OVERLAP_LINES);
                chunks.add(overlap + "\n\n" + rawChunks.get(i));
            }
        }

        return List.copyOf(chunks);
    }

    private String getTrailingLines(String text, int lineCount) {
        String[] lines = text.split("\n");
        int start = Math.max(0, lines.length - lineCount);
        return String.join("\n", java.util.Arrays.copyOfRange(lines, start, lines.length));
    }
}