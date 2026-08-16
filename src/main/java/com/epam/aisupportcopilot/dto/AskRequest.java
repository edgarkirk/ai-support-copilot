package com.epam.aisupportcopilot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AskRequest(
    @NotBlank(message = "Question must not be empty")
    @Size(max = 2000, message = "Question must not exceed 2000 characters")
    String question
) {
}