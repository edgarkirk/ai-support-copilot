package com.epam.aisupportcopilot.service.ai;

import com.epam.aisupportcopilot.dto.EvaluationResult;
import com.epam.aisupportcopilot.dto.EvaluationResult.TestCaseResult;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

/**
 * Automated evaluation suite using the LLM-as-judge pattern.
 *
 * <p>Runs predefined test cases through the full AI pipeline (tool calling + RAG),
 * then asks a separate LLM call to score each answer on a 1–5 scale.
 * A score of {@value #PASS_THRESHOLD} or higher is considered a pass.
 *
 * <p>Covers ticket data queries, knowledge base retrieval, incident postmortems,
 * policy questions, and off-topic rejection.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RagEvaluationService {

    private static final String EVAL_SESSION_PREFIX = "eval-";
    private static final int PASS_THRESHOLD = 3;

    private final AiService aiService;
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    private static final String JUDGE_PROMPT = """
            You are an evaluation judge. Score the following answer on a scale of 1-5:
            1 = Completely wrong, irrelevant, or fabricated
            2 = Partially relevant but missing key information or contains errors
            3 = Acceptable — addresses the question with mostly correct information
            4 = Good — accurate, relevant, and well-structured
            5 = Excellent — accurate, comprehensive, cites sources, actionable

            For off-topic rejection questions (e.g. "How do I make pasta?"), score 5 if the answer politely declines and explains it only handles support topics, score 1 if it actually answers the off-topic question.

            Question: %s
            Expected to cover: %s
            Actual answer: %s

            Respond with EXACTLY this format (nothing else):
            SCORE: <number>
            REASONING: <one sentence explanation>
            """;

    private static final List<TestCase> TEST_CASES = List.of(
            new TestCase(
                    "How many authentication tickets were there in July 2026?",
                    "Should use ticket data tools, return a specific count for AUTHENTICATION category in July 2026"
            ),
            new TestCase(
                    "What should support do when authentication failures increase?",
                    "Should search knowledge base, mention troubleshooting steps like checking sessions, passwords, escalation procedures"
            ),
            new TestCase(
                    "What happened during the authentication incident in March 2026?",
                    "Should reference the postmortem, mention stale session cache, 401 errors, deployment as root cause"
            ),
            new TestCase(
                    "What is the SLA response time for critical incidents?",
                    "Should cite SLA policy document, mention 15-minute response time for S1/Critical severity"
            ),
            new TestCase(
                    "A customer is not receiving order confirmation emails. What should I do?",
                    "Should search knowledge base for notification troubleshooting, mention checking delivery status, queued/failed states"
            ),
            new TestCase(
                    "How do I handle a locked customer account?",
                    "Should reference account troubleshooting, mention lockout duration, failed login attempts, Tier 2 escalation"
            ),
            new TestCase(
                    "What is the refund policy?",
                    "Should cite refund policy document, mention 30-day window, eligible scenarios, exceptions"
            ),
            new TestCase(
                    "What happened during the payment incident in July 2026?",
                    "Should reference payment postmortem, mention gateway timeout, latency issues, duplicate charges"
            ),
            new TestCase(
                    "How do I make pasta?",
                    "Should politely decline as off-topic and explain it only handles support-related questions"
            ),
            new TestCase(
                    "How does the notification service handle failed deliveries?",
                    "Should reference notification service docs, mention retry with exponential backoff, failure handling"
            )
    );

    public EvaluationResult evaluate() {
        log.info("Starting LLM-as-judge evaluation with {} test cases", TEST_CASES.size());

        List<TestCaseResult> results = new ArrayList<>();
        int passed = 0;

        for (int i = 0; i < TEST_CASES.size(); i++) {
            TestCase tc = TEST_CASES.get(i);
            String sessionId = EVAL_SESSION_PREFIX + i;

            log.info("Evaluating [{}/{}]: {}", i + 1, TEST_CASES.size(), tc.question());

            try {
                String answer = aiService.ask(sessionId, tc.question()).content();
                chatMemory.clear(sessionId);

                TestCaseResult result = judgeAnswer(tc, answer);
                results.add(result);
                if (result.passed()) {
                    passed++;
                }
                log.info("  Result: {} (score: {}/5 — {})",
                        result.passed() ? "PASS" : "FAIL",
                        result.llmScore(),
                        result.llmReasoning());
            } catch (Exception e) {
                log.error("  Error evaluating test case", e);
                results.add(new TestCaseResult(
                        tc.question(), false, 0,
                        "ERROR: " + e.getMessage(), ""));
            }
        }

        double score = (double) passed / TEST_CASES.size();
        log.info("Evaluation complete: {}/{} passed (score: {}%)",
                passed, TEST_CASES.size(), Math.round(score * 100));

        return new EvaluationResult(TEST_CASES.size(), passed,
                TEST_CASES.size() - passed, score, results);
    }

    private TestCaseResult judgeAnswer(TestCase tc, String answer) {
        String judgeInput = String.format(JUDGE_PROMPT,
                tc.question(), tc.expectedBehavior(), answer);

        String judgment = chatClient.prompt()
                .user(judgeInput)
                .call()
                .content();

        int llmScore = parseScore(judgment);
        String reasoning = parseReasoning(judgment);
        boolean passed = llmScore >= PASS_THRESHOLD;

        return new TestCaseResult(tc.question(), passed, llmScore, reasoning, answer);
    }

    private int parseScore(String judgment) {
        Matcher matcher = Pattern.compile("SCORE:\\s*(\\d)").matcher(judgment);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    private String parseReasoning(String judgment) {
        Matcher matcher = Pattern.compile("REASONING:\\s*(.+)").matcher(judgment);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return judgment.trim();
    }

    private record TestCase(String question, String expectedBehavior) {}
}