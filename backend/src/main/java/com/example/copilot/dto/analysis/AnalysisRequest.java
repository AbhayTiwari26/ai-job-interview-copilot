package com.example.copilot.dto.analysis;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnalysisRequest {

    @NotNull(message = "Resume ID is required")
    private Long resumeId;

    @NotNull(message = "Job description ID is required")
    private Long jobDescriptionId;
}
