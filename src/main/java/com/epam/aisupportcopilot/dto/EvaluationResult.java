package com.epam.aisupportcopilot.dto;

import java.util.List;

public record EvaluationResult(
        int totalCases,
        int passed,
        int failed,
        double score,
        List<TestCaseResult> details
) {
    public record TestCaseResult(
            String question,
            boolean passed,
            int llmScore,
            String llmReasoning,
            String answer
    ) {}
}