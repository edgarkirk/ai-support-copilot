package com.epam.aisupportcopilot.dto;

public sealed interface StreamEvent {

    record ToolCall(String toolName) implements StreamEvent {}

    record Content(String text) implements StreamEvent {}
}