package com.epam.aisupportcopilot.controller;

import com.epam.aisupportcopilot.dto.AskRequest;
import com.epam.aisupportcopilot.dto.StreamEvent;
import com.epam.aisupportcopilot.service.AiService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private static final long SSE_TIMEOUT_MS = 120_000L;

    private final AiService aiService;

    @PostMapping("/ask")
    public String ask(@Valid @RequestBody AskRequest request, HttpSession session) {
        return aiService.ask(session.getId(), request.question());
    }

    @PostMapping("/ask/stream")
    public SseEmitter askStream(@Valid @RequestBody AskRequest request, HttpSession session) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        aiService.askStream(session.getId(), request.question())
                .subscribe(
                        event -> {
                            try {
                                switch (event) {
                                    case StreamEvent.ToolCall(var toolName) -> emitter.send(
                                            SseEmitter.event().name("tool_call").data(toolName));
                                    case StreamEvent.Content(var text) -> emitter.send(
                                            SseEmitter.event().name("content").data(text));
                                }
                            } catch (IOException e) {
                                log.error("Error sending SSE event", e);
                                emitter.completeWithError(e);
                            }
                        },
                        error -> {
                            try {
                                log.error("Stream error", error);
                                emitter.send(SseEmitter.event()
                                        .name("error")
                                        .data("An error occurred while processing your request."));
                                emitter.complete();
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        () -> {
                            try {
                                emitter.send(SseEmitter.event().name("done").data(""));
                                emitter.complete();
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        }
                );

        return emitter;
    }
}