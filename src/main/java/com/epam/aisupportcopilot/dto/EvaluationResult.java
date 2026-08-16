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
            List<String> expectedKeywords,
            List<String> matchedKeywords,
            List<String> missedKeywords,
            boolean passed,
            String answer
    ) {}
}