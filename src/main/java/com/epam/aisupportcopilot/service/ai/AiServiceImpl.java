package com.epam.aisupportcopilot.service.ai;

import com.epam.aisupportcopilot.dto.AskResponse;
import com.epam.aisupportcopilot.tools.KnowledgeTools;
import com.epam.aisupportcopilot.tools.TicketTools;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * Orchestrates the LLM interaction pipeline: wraps tool callbacks with
 * {@link ObservingToolCallback} to track invocations, injects the system prompt
 * with the current date, attaches per-session conversation memory, and measures
 * end-to-end response time.
 */
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
    public AskResponse ask(String sessionId, String question) {
        long startTime = System.currentTimeMillis();

        List<String> toolCallNames = Collections.synchronizedList(new ArrayList<>());

        ToolCallback[] callbacks = Arrays.stream(
                MethodToolCallbackProvider.builder()
                        .toolObjects(ticketTools, knowledgeTools)
                        .build()
                        .getToolCallbacks())
                .map(cb -> (ToolCallback) new ObservingToolCallback(cb, name -> toolCallNames.add(name)))
                .toArray(ToolCallback[]::new);

        String content = chatClient.prompt()
                .system(s -> s.text(systemPromptResource)
                        .param("currentDate", LocalDate.now(ZoneId.systemDefault()).toString()))
                .user(question)
                .toolCallbacks(callbacks)
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId(sessionId)
                        .build())
                .call()
                .content();

        long durationMs = System.currentTimeMillis() - startTime;
        return new AskResponse(List.copyOf(toolCallNames), content, durationMs);
    }
}