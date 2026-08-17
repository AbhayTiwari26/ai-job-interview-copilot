package com.example.copilot.dto.interview;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InterviewGenerateRequest {

    @NotNull(message = "Analysis ID is required")
    private Long analysisId;
}
