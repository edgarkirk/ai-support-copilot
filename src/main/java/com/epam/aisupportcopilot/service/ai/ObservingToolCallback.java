package com.epam.aisupportcopilot.service.ai;

import java.util.function.Consumer;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * Decorator around {@link ToolCallback} that notifies a consumer each time a tool is invoked.
 *
 * <p>Used by {@link AiServiceImpl} to collect tool call names for inclusion in the
 * {@link com.epam.aisupportcopilot.dto.AskResponse} without modifying the tool execution logic.
 */
class ObservingToolCallback implements ToolCallback {

    private final ToolCallback delegate;
    private final Consumer<String> onToolCall;

    ObservingToolCallback(ToolCallback delegate, Consumer<String> onToolCall) {
        this.delegate = delegate;
        this.onToolCall = onToolCall;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public String call(String toolInput) {
        onToolCall.accept(getToolDefinition().name());
        return delegate.call(toolInput);
    }

    @Override
    public String call(String toolInput, org.springframework.ai.chat.model.ToolContext toolContext) {
        onToolCall.accept(getToolDefinition().name());
        return delegate.call(toolInput, toolContext);
    }
}