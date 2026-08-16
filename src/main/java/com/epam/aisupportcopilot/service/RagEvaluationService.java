package com.epam.aisupportcopilot.service;

import com.epam.aisupportcopilot.dto.EvaluationResult;
import com.epam.aisupportcopilot.dto.EvaluationResult.TestCaseResult;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagEvaluationService {

    private static final String EVAL_SESSION_PREFIX = "eval-";
    private static final double PASS_THRESHOLD = 0.5;

    private final AiService aiService;
    private final ChatMemory chatMemory;

    private static final List<TestCase> TEST_CASES = List.of(
            new TestCase(
                    "How many authentication tickets were there in July 2026?",
                    List.of("AUTHENTICATION", "July", "2026")
            ),
            new TestCase(
                    "What should support do when authentication failures increase?",
                    List.of("troubleshoot", "escalat", "session", "password")
            ),
            new TestCase(
                    "What happened during the authentication incident in March 2026?",
                    List.of("session", "cache", "401", "password", "deployment")
            ),
            new TestCase(
                    "What is the SLA response time for critical incidents?",
                    List.of("15 minute", "4 hour", "Critical", "S1")
            ),
            new TestCase(
                    "A customer is not receiving order confirmation emails. What should I do?",
                    List.of("notification", "email", "QUEUED", "FAILED", "delay")
            ),
            new TestCase(
                    "How do I handle a locked customer account?",
                    List.of("lock", "30 minute", "failed login", "Tier 2")
            ),
            new TestCase(
                    "What is the refund policy?",
                    List.of("30", "day", "refund", "duplicate", "outage")
            ),
            new TestCase(
                    "What happened during the payment incident in July 2026?",
                    List.of("gateway", "timeout", "latency", "duplicate", "charge")
            ),
            new TestCase(
                    "How do I make pasta?",
                    List.of("only help with support", "support-related")
            ),
            new TestCase(
                    "How does the notification service handle failed deliveries?",
                    List.of("retry", "exponential", "backoff", "FAILED")
            )
    );

    public EvaluationResult evaluate() {
        log.info("Starting RAG evaluation with {} test cases", TEST_CASES.size());

        List<TestCaseResult> results = new ArrayList<>();
        int passed = 0;

        for (int i = 0; i < TEST_CASES.size(); i++) {
            TestCase tc = TEST_CASES.get(i);
            String sessionId = EVAL_SESSION_PREFIX + i;

            log.info("Evaluating [{}/{}]: {}", i + 1, TEST_CASES.size(), tc.question());

            try {
                String answer = aiService.ask(sessionId, tc.question());
                chatMemory.clear(sessionId);
                TestCaseResult result = evaluateAnswer(tc, answer);
                results.add(result);
                if (result.passed()) {
                    passed++;
                }
                log.info("  Result: {} ({}/{}  keywords matched)",
                        result.passed() ? "PASS" : "FAIL",
                        result.matchedKeywords().size(),
                        tc.expectedKeywords().size());
            } catch (Exception e) {
                log.error("  Error evaluating test case", e);
                results.add(new TestCaseResult(
                        tc.question(), tc.expectedKeywords(),
                        List.of(), tc.expectedKeywords(), false,
                        "ERROR: " + e.getMessage()));
            }
        }

        double score = (double) passed / TEST_CASES.size();
        log.info("Evaluation complete: {}/{} passed (score: {}%)", passed, TEST_CASES.size(), Math.round(score * 100));

        return new EvaluationResult(TEST_CASES.size(), passed, TEST_CASES.size() - passed, score, results);
    }

    private TestCaseResult evaluateAnswer(TestCase tc, String answer) {
        String answerLower = answer.toLowerCase();

        List<String> matched = new ArrayList<>();
        List<String> missed = new ArrayList<>();

        for (String keyword : tc.expectedKeywords()) {
            if (answerLower.contains(keyword.toLowerCase())) {
                matched.add(keyword);
            } else {
                missed.add(keyword);
            }
        }

        double matchRatio = (double) matched.size() / tc.expectedKeywords().size();
        boolean passed = matchRatio >= PASS_THRESHOLD;

        return new TestCaseResult(tc.question(), tc.expectedKeywords(), matched, missed, passed, answer);
    }

    private record TestCase(String question, List<String> expectedKeywords) {}
}