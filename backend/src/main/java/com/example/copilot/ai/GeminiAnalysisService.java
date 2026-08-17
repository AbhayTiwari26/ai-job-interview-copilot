package com.example.copilot.ai;

import com.example.copilot.dto.analysis.AnalysisAiResult;
import com.example.copilot.exception.AIServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiAnalysisService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AnalysisAiResult analyzeResumeVsJob(String resumeText, String jobTitle, String jobDescription) {
        String prompt = buildPrompt(resumeText, jobTitle, jobDescription);

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.debug("AI Analysis raw response received.");
            return parseResponse(response);

        } catch (AIServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Gemini API call failed for analysis: {}", e.getMessage());
            throw new AIServiceException("AI analysis service is temporarily unavailable. Please try again.");
        }
    }

    private String buildPrompt(String resumeText, String jobTitle, String jobDescription) {
        return """
                You are a professional resume evaluator. Analyze the resume against the job description.

                STRICT RULES:
                - Use ONLY information explicitly stated in the resume.
                - Do NOT invent qualifications, skills, or experience.
                - If the resume does not mention a skill, treat it as missing.
                - Return ONLY valid JSON. No markdown, no code blocks, no explanation text.

                RESUME:
                """ + resumeText + """

                JOB TITLE: """ + jobTitle + """

                JOB DESCRIPTION:
                """ + jobDescription + """

                Return ONLY this exact JSON structure (no extra text):
                {
                  "matchScore": <integer 0-100>,
                  "matchedSkills": ["skill1", "skill2"],
                  "missingSkills": ["skill1", "skill2"],
                  "skillAnalysis": {
                    "technical": <integer 0-100>,
                    "experience": <integer 0-100>,
                    "projects": <integer 0-100>,
                    "keywords": <integer 0-100>
                  },
                  "strengths": ["strength1", "strength2"],
                  "recommendations": ["recommendation1", "recommendation2"]
                }
                """;
    }

    private AnalysisAiResult parseResponse(String response) {
        String cleaned = cleanJson(response);
        try {
            AnalysisAiResult result = objectMapper.readValue(cleaned, AnalysisAiResult.class);
            validateResult(result);
            return result;
        } catch (AIServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse AI analysis response. Cleaned response: {}", cleaned);
            throw new AIServiceException("Received an unexpected response format from AI. Please try again.");
        }
    }

    private void validateResult(AnalysisAiResult result) {
        if (result.getMatchScore() == null) {
            throw new AIServiceException("AI returned incomplete analysis data. Please try again.");
        }
        if (result.getMatchScore() < 0 || result.getMatchScore() > 100) {
            result.setMatchScore(Math.max(0, Math.min(100, result.getMatchScore())));
        }
    }

    private String cleanJson(String response) {
        if (response == null) return "{}";
        return response
                .replaceAll("(?s)```json\\s*", "")
                .replaceAll("(?s)```\\s*", "")
                .trim();
    }
}
