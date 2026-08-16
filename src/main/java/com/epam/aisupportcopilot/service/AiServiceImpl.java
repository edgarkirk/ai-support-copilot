package com.epam.aisupportcopilot.service;

import com.epam.aisupportcopilot.dto.StreamEvent;
import com.epam.aisupportcopilot.tools.KnowledgeTools;
import com.epam.aisupportcopilot.tools.ObservableToolCallback;
import com.epam.aisupportcopilot.tools.TicketTools;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final ChatClient chatClient;
    private final TicketTools ticketTools;
    private final KnowledgeTools knowledgeTools;
    private final ChatMemory chatMemory;

    @Value("classpath:prompts/system-prompt.txt")
    private Resource systemPromptResource;

    @Override
    public String ask(String sessionId, String question) {
        return chatClient.prompt()
                .system(s -> s.text(systemPromptResource)
                        .param("currentDate", LocalDate.now(ZoneId.systemDefault()).toString()))
                .user(question)
                .tools(ticketTools, knowledgeTools)
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId(sessionId)
                        .build())
                .call()
                .content();
    }

    @Override
    public Flux<StreamEvent> askStream(String sessionId, String question) {
        return Flux.defer(() -> {
            Sinks.Many<StreamEvent> toolCallSink = Sinks.many().multicast().onBackpressureBuffer();

            ToolCallback[] originalCallbacks = MethodToolCallbackProvider.builder()
                    .toolObjects(ticketTools, knowledgeTools)
                    .build()
                    .getToolCallbacks();

            ToolCallback[] observableCallbacks = Arrays.stream(originalCallbacks)
                    .map(cb -> (ToolCallback) new ObservableToolCallback(cb,
                            toolName -> toolCallSink.tryEmitNext(new StreamEvent.ToolCall(toolName))))
                    .toArray(ToolCallback[]::new);

            Flux<StreamEvent> contentStream = chatClient.prompt()
                    .system(s -> s.text(systemPromptResource)
                            .param("currentDate", LocalDate.now(ZoneId.systemDefault()).toString()))
                    .user(question)
                    .toolCallbacks(observableCallbacks)
                    .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                            .conversationId(sessionId)
                            .build())
                    .stream()
                    .content()
                    .filter(text -> !text.isEmpty())
                    .map(text -> (StreamEvent) new StreamEvent.Content(text))
                    .doOnComplete(toolCallSink::tryEmitComplete)
                    .doOnError(e -> toolCallSink.tryEmitComplete());

            return Flux.merge(toolCallSink.asFlux(), contentStream);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}