package com.epam.aisupportcopilot.service.ai;

import com.epam.aisupportcopilot.dto.AskResponse;

/**
 * Service for processing user questions through the LLM with tool-calling support.
 */
public interface AiService {

    /**
     * Sends a user question to the LLM, which may invoke ticket and knowledge tools,
     * and returns the generated answer along with metadata (tool calls used, duration).
     *
     * @param sessionId HTTP session ID used for per-session conversation memory
     * @param question  the user's natural-language question
     * @return response containing the LLM answer, list of invoked tools, and duration in ms
     */
    AskResponse ask(String sessionId, String question);
}