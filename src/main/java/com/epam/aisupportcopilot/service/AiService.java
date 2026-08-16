package com.epam.aisupportcopilot.service;

import com.epam.aisupportcopilot.dto.StreamEvent;
import reactor.core.publisher.Flux;

public interface AiService {

    String ask(String sessionId, String question);

    Flux<StreamEvent> askStream(String sessionId, String question);
}