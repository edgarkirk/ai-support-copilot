package com.epam.aisupportcopilot.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SectionChunkingStrategy implements ChunkingStrategy {

    @Override
    public List<String> chunk(String content) {
        String[] sections = content.split("(?m)(?=^#{1,2} )");

        List<String> chunks = new ArrayList<>();
        for (String section : sections) {
            String trimmed = section.trim();
            if (!trimmed.isEmpty()) {
                chunks.add(trimmed);
            }
        }

        if (chunks.isEmpty()) {
            chunks.add(content.trim());
        }

        return List.copyOf(chunks);
    }
}