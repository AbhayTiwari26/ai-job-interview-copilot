package com.example.copilot.service;

import com.example.copilot.ai.GeminiAnalysisService;
import com.example.copilot.dto.analysis.AnalysisAiResult;
import com.example.copilot.dto.analysis.AnalysisRequest;
import com.example.copilot.dto.analysis.AnalysisResponse;
import com.example.copilot.entity.Analysis;
import com.example.copilot.entity.JobDescription;
import com.example.copilot.entity.Resume;
import com.example.copilot.entity.User;
import com.example.copilot.exception.AIServiceException;
import com.example.copilot.exception.BadRequestException;
import com.example.copilot.exception.ResourceNotFoundException;
import com.example.copilot.repository.AnalysisRepository;
import com.example.copilot.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final ResumeService resumeService;
    private final JobService jobService;
    private final GeminiAnalysisService geminiAnalysisService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public AnalysisResponse createAnalysis(AnalysisRequest request, String userEmail) {
        Resume resume = resumeService.getResumeEntity(request.getResumeId(), userEmail);

        if (resume.getExtractedText() == null || resume.getExtractedText().isBlank()) {
            throw new BadRequestException(
                    "The resume has no extractable text. Please upload a text-based PDF, not a scanned image.");
        }

        JobDescription job = jobService.getJobEntity(request.getJobDescriptionId(), userEmail);

        AnalysisAiResult aiResult = geminiAnalysisService.analyzeResumeVsJob(
                resume.getExtractedText(),
                job.getTitle(),
                job.getDescription()
        );

        String analysisJson = serializeToJson(aiResult);

        Analysis analysis = Analysis.builder()
                .resume(resume)
                .jobDescription(job)
                .matchScore(aiResult.getMatchScore())
                .analysisJson(analysisJson)
                .build();

        analysis = analysisRepository.save(analysis);
        log.info("Analysis created: id={}, score={}", analysis.getId(), analysis.getMatchScore());

        return toResponse(analysis, aiResult);
    }

    @Transactional(readOnly = true)
    public AnalysisResponse getAnalysis(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);
        Analysis analysis = analysisRepository.findByIdAndResumeUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found."));

        AnalysisAiResult aiResult = deserializeJson(analysis.getAnalysisJson());
        return toResponse(analysis, aiResult);
    }

    @Transactional(readOnly = true)
    public List<AnalysisResponse> getAnalyses(String userEmail) {
        User user = getUserByEmail(userEmail);
        return analysisRepository.findByResumeUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(a -> toResponse(a, deserializeJson(a.getAnalysisJson())))
                .collect(Collectors.toList());
    }

    /** Used internally by InterviewService to get the stored analysis result. */
    public AnalysisAiResult getAnalysisAiResult(Analysis analysis) {
        return deserializeJson(analysis.getAnalysisJson());
    }

    private AnalysisResponse toResponse(Analysis analysis, AnalysisAiResult aiResult) {
        AnalysisResponse.SkillAnalysisDto skillDto = null;
        if (aiResult.getSkillAnalysis() != null) {
            skillDto = AnalysisResponse.SkillAnalysisDto.builder()
                    .technical(aiResult.getSkillAnalysis().getTechnical())
                    .experience(aiResult.getSkillAnalysis().getExperience())
                    .projects(aiResult.getSkillAnalysis().getProjects())
                    .keywords(aiResult.getSkillAnalysis().getKeywords())
                    .build();
        }

        return AnalysisResponse.builder()
                .id(analysis.getId())
                .resumeId(analysis.getResume().getId())
                .resumeFilename(analysis.getResume().getFilename())
                .jobDescriptionId(analysis.getJobDescription().getId())
                .jobTitle(analysis.getJobDescription().getTitle())
                .matchScore(analysis.getMatchScore())
                .matchedSkills(safeList(aiResult.getMatchedSkills()))
                .missingSkills(safeList(aiResult.getMissingSkills()))
                .skillAnalysis(skillDto)
                .strengths(safeList(aiResult.getStrengths()))
                .recommendations(safeList(aiResult.getRecommendations()))
                .createdAt(analysis.getCreatedAt())
                .build();
    }

    private String serializeToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new AIServiceException("Failed to serialize AI result.");
        }
    }

    private AnalysisAiResult deserializeJson(String json) {
        try {
            return objectMapper.readValue(json, AnalysisAiResult.class);
        } catch (Exception e) {
            log.error("Failed to deserialize stored analysis JSON");
            return new AnalysisAiResult();
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private <T> List<T> safeList(List<T> list) {
        return list != null ? list : Collections.emptyList();
    }
}
