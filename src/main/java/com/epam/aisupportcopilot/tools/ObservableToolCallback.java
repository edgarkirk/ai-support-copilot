package com.epam.aisupportcopilot.tools;

import java.util.function.Consumer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

public class ObservableToolCallback implements ToolCallback {

    private final ToolCallback delegate;
    private final Consumer<String> onToolCall;

    public ObservableToolCallback(ToolCallback delegate, Consumer<String> onToolCall) {
        this.delegate = delegate;
        this.onToolCall = onToolCall;
    }

    @NonNull
    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @NonNull
    @Override
    public ToolMetadata getToolMetadata() {
        return delegate.getToolMetadata();
    }

    @NonNull
    @Override
    public String call(@NonNull String toolInput) {
        onToolCall.accept(getToolDefinition().name());
        return delegate.call(toolInput);
    }

    @NonNull
    @Override
    public String call(@NonNull String toolInput, @Nullable ToolContext toolContext) {
        onToolCall.accept(getToolDefinition().name());
        return delegate.call(toolInput, toolContext);
    }
}