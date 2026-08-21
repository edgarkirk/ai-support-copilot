package com.epam.aisupportcopilot.dto;

import java.util.List;

public record AskResponse(List<String> toolCalls, String content, long durationMs) {
}