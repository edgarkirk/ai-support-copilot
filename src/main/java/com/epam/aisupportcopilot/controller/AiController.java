package com.epam.aisupportcopilot.controller;

import com.epam.aisupportcopilot.dto.AskRequest;
import com.epam.aisupportcopilot.dto.AskResponse;
import com.epam.aisupportcopilot.service.ai.AiService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint for the AI support copilot. Accepts user questions and returns
 * LLM-generated answers enriched with ticket data and knowledge base context.
 * Each HTTP session maintains its own conversation memory.
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/ask")
    public AskResponse ask(@Valid @RequestBody AskRequest request, HttpSession session) {
        return aiService.ask(session.getId(), request.question());
    }
}