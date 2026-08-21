package com.epam.aisupportcopilot.controller;

import com.epam.aisupportcopilot.dto.EvaluationResult;
import com.epam.aisupportcopilot.service.ai.RagEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/evaluation")
@RequiredArgsConstructor
public class EvaluationController {

    private final RagEvaluationService ragEvaluationService;

    @PostMapping("/run")
    public EvaluationResult runEvaluation() {
        return ragEvaluationService.evaluate();
    }
}