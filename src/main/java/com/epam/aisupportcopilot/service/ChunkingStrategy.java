package com.epam.aisupportcopilot.service;

import java.util.List;

public interface ChunkingStrategy {

    List<String> chunk(String content);
}