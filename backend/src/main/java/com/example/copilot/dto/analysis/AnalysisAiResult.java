package com.example.copilot.dto.analysis;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Internal POJO used to deserialize the AI's JSON response for resume/JD analysis.
 * Not exposed directly via REST API — converted to AnalysisResponse for the frontend.
 */
@Data
@NoArgsConstructor
public class AnalysisAiResult {

    private Integer matchScore;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private SkillAnalysis skillAnalysis;
    private List<String> strengths;
    private List<String> recommendations;

    @Data
    @NoArgsConstructor
    public static class SkillAnalysis {
        private Integer technical;
        private Integer experience;
        private Integer projects;
        private Integer keywords;
    }
}
