package com.epam.aisupportcopilot.config;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import java.nio.charset.StandardCharsets;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.model.azure.openai.autoconfigure.AzureOpenAIClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    /**
     * Workaround for {@code gpt-5-mini} not supporting the {@code logprobs} parameter.
     * Spring AI 1.1.2 unconditionally includes {@code logprobs} and {@code top_logprobs}
     * in the request body, causing {@code gpt-5.6-sol-2026-07-09} to reject requests with HTTP 422.
     * Other models (e.g. {@code gpt-4o, gpt-5}) are not affected.
     *
     * <p>This customizer injects an HTTP pipeline policy that strips both fields
     * from the JSON request body before it reaches the Azure API.
     *
     * <p>Can be removed if switching to a model that supports {@code logprobs},
     * or once Spring AI adds a way to opt out of sending it.
     */
    @Bean
    public AzureOpenAIClientBuilderCustomizer stripLogprobsCustomizer() {
        return builder -> builder.addPolicy(new StripLogprobsPolicy());
    }

    private static class StripLogprobsPolicy implements HttpPipelinePolicy {

        private static final String LOGPROBS_PATTERN = "\"logprobs\":(null|true|false),?";
        private static final String TOP_LOGPROBS_PATTERN = ",?\"top_logprobs\":\\d+";

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            return stripLogprobs(context).then(next.process());
        }

        @Override
        public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
            stripLogprobsSync(context);
            return next.processSync();
        }

        private Mono<Void> stripLogprobs(HttpPipelineCallContext context) {
            var request = context.getHttpRequest();
            var body = request.getBody();
            if (body == null) {
                return Mono.empty();
            }
            return body.reduce((b1, b2) -> {
                var combined = new byte[b1.remaining() + b2.remaining()];
                b1.get(combined, 0, b1.remaining());
                b2.get(combined, b1.capacity(), b2.remaining());
                return java.nio.ByteBuffer.wrap(combined);
            }).flatMap(buffer -> {
                var json = StandardCharsets.UTF_8.decode(buffer).toString();
                var cleaned = cleanJson(json);
                request.setBody(cleaned.getBytes(StandardCharsets.UTF_8));
                request.getHeaders().set(HttpHeaderName.CONTENT_LENGTH,
                    String.valueOf(cleaned.getBytes(StandardCharsets.UTF_8).length));
                return Mono.empty();
            });
        }

        private void stripLogprobsSync(HttpPipelineCallContext context) {
            var request = context.getHttpRequest();
            var body = request.getBodyAsBinaryData();
            if (body == null) {
                return;
            }
            var json = body.toString();
            var cleaned = cleanJson(json);
            request.setBody(cleaned.getBytes(StandardCharsets.UTF_8));
            request.getHeaders().set(HttpHeaderName.CONTENT_LENGTH,
                String.valueOf(cleaned.getBytes(StandardCharsets.UTF_8).length));
        }

        private String cleanJson(String json) {
            return json.replaceAll(LOGPROBS_PATTERN, "")
                .replaceAll(TOP_LOGPROBS_PATTERN, "")
                .replace(",}", "}")
                .replace(",]", "]");
        }
    }

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
    }
}