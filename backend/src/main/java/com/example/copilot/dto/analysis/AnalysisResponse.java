package com.example.copilot.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AnalysisResponse {
    private Long id;
    private Long resumeId;
    private String resumeFilename;
    private Long jobDescriptionId;
    private String jobTitle;
    private Integer matchScore;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private SkillAnalysisDto skillAnalysis;
    private List<String> strengths;
    private List<String> recommendations;
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SkillAnalysisDto {
        private Integer technical;
        private Integer experience;
        private Integer projects;
        private Integer keywords;
    }
}
