package com.example.copilot.dto.interview;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Internal POJO for deserializing Gemini's answer evaluation JSON response.
 * Also used as the feedback object in InterviewResponse.
 */
@Data
@NoArgsConstructor
public class EvaluationResult {
    private Integer overallScore;
    private Integer technicalAccuracy;
    private Integer completeness;
    private Integer communication;
    private List<String> strengths;
    private List<String> missingPoints;
    private List<String> improvements;
    private String betterAnswerStructure;
}
