package com.example.copilot.dto.interview;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InterviewResponse {
    private Long id;
    private Long analysisId;
    private String question;
    private String category;
    private String difficulty;
    private String answer;
    private Integer score;
    private EvaluationResult feedback;
    private LocalDateTime createdAt;
}
