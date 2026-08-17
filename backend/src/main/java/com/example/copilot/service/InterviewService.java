package com.example.copilot.service;

import com.example.copilot.ai.GeminiEvaluationService;
import com.example.copilot.ai.GeminiInterviewService;
import com.example.copilot.dto.analysis.AnalysisAiResult;
import com.example.copilot.dto.interview.*;
import com.example.copilot.entity.Analysis;
import com.example.copilot.entity.Interview;
import com.example.copilot.entity.User;
import com.example.copilot.exception.AIServiceException;
import com.example.copilot.exception.BadRequestException;
import com.example.copilot.exception.ResourceNotFoundException;
import com.example.copilot.repository.AnalysisRepository;
import com.example.copilot.repository.InterviewRepository;
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
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final AnalysisRepository analysisRepository;
    private final UserRepository userRepository;
    private final GeminiInterviewService geminiInterviewService;
    private final GeminiEvaluationService geminiEvaluationService;
    private final AnalysisService analysisService;
    private final ObjectMapper objectMapper;

    @Transactional
    public List<InterviewResponse> generateQuestions(InterviewGenerateRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        Analysis analysis = analysisRepository.findByIdAndResumeUserId(request.getAnalysisId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found."));

        AnalysisAiResult aiResult = analysisService.getAnalysisAiResult(analysis);

        List<GeneratedQuestion> questions = geminiInterviewService.generateQuestions(
                analysis.getResume().getExtractedText(),
                analysis.getJobDescription().getTitle(),
                analysis.getJobDescription().getDescription(),
                safeList(aiResult.getMatchedSkills()),
                safeList(aiResult.getMissingSkills())
        );

        List<Interview> savedInterviews = questions.stream()
                .map(q -> Interview.builder()
                        .analysis(analysis)
                        .question(q.getQuestion())
                        .category(q.getCategory() != null ? q.getCategory() : "GENERAL")
                        .build())
                .map(interviewRepository::save)
                .collect(Collectors.toList());

        log.info("Generated {} interview questions for analysisId={}", savedInterviews.size(), analysis.getId());
        return savedInterviews.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public InterviewResponse submitAnswer(Long interviewId, AnswerRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        Interview interview = interviewRepository.findByIdAndUserId(interviewId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Interview question not found."));

        if (interview.getAnswer() != null) {
            throw new BadRequestException("This question has already been answered.");
        }

        String jobContext = interview.getAnalysis().getJobDescription().getTitle()
                + " — "
                + truncate(interview.getAnalysis().getJobDescription().getDescription(), 500);

        EvaluationResult evaluation = geminiEvaluationService.evaluateAnswer(
                interview.getQuestion(),
                interview.getCategory(),
                request.getAnswer(),
                jobContext
        );

        interview.setAnswer(request.getAnswer());
        interview.setScore(evaluation.getOverallScore());
        interview.setFeedbackJson(serializeToJson(evaluation));

        interview = interviewRepository.save(interview);
        log.info("Answer submitted for interviewId={}, score={}", interviewId, interview.getScore());

        return toResponseWithFeedback(interview, evaluation);
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getInterviewsByAnalysis(Long analysisId, String userEmail) {
        User user = getUserByEmail(userEmail);

        Analysis analysis = analysisRepository.findByIdAndResumeUserId(analysisId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found."));

        return interviewRepository.findByAnalysisIdAndUserId(analysis.getId(), user.getId())
                .stream()
                .map(i -> {
                    if (i.getFeedbackJson() != null) {
                        return toResponseWithFeedback(i, deserializeFeedback(i.getFeedbackJson()));
                    }
                    return toResponse(i);
                })
                .collect(Collectors.toList());
    }

    private InterviewResponse toResponse(Interview interview) {
        return InterviewResponse.builder()
                .id(interview.getId())
                .analysisId(interview.getAnalysis().getId())
                .question(interview.getQuestion())
                .category(interview.getCategory())
                .answer(interview.getAnswer())
                .score(interview.getScore())
                .createdAt(interview.getCreatedAt())
                .build();
    }

    private InterviewResponse toResponseWithFeedback(Interview interview, EvaluationResult evaluation) {
        return InterviewResponse.builder()
                .id(interview.getId())
                .analysisId(interview.getAnalysis().getId())
                .question(interview.getQuestion())
                .category(interview.getCategory())
                .answer(interview.getAnswer())
                .score(interview.getScore())
                .feedback(evaluation)
                .createdAt(interview.getCreatedAt())
                .build();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private String serializeToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new AIServiceException("Failed to serialize evaluation result.");
        }
    }

    private EvaluationResult deserializeFeedback(String json) {
        try {
            return objectMapper.readValue(json, EvaluationResult.class);
        } catch (Exception e) {
            return new EvaluationResult();
        }
    }

    private <T> List<T> safeList(List<T> list) {
        return list != null ? list : Collections.emptyList();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }
}
