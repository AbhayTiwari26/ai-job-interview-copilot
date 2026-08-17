package com.example.copilot.ai;

import com.example.copilot.dto.interview.EvaluationResult;
import com.example.copilot.exception.AIServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiEvaluationService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public EvaluationResult evaluateAnswer(String question, String category, String answer, String jobContext) {
        String prompt = buildPrompt(question, category, answer, jobContext);

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.debug("AI Evaluation raw response received.");
            return parseResponse(response);

        } catch (AIServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Gemini API call failed for answer evaluation: {}", e.getMessage());
            throw new AIServiceException("Answer evaluation service is temporarily unavailable. Please try again.");
        }
    }

    private String buildPrompt(String question, String category, String answer, String jobContext) {
        return """
                You are an expert interview evaluator. Evaluate the candidate's answer.

                STRICT RULES:
                - Evaluate ONLY what is explicitly stated in the candidate's answer.
                - Do NOT fabricate facts or assume knowledge the candidate did not demonstrate.
                - Be constructive, specific, and actionable in your feedback.
                - Score on a scale of 0-10 where 10 is a perfect answer.
                - Return ONLY valid JSON. No markdown, no explanation text.

                QUESTION: """ + question + """

                CATEGORY: """ + category + """

                JOB CONTEXT: """ + jobContext + """

                CANDIDATE'S ANSWER:
                """ + answer + """

                Return ONLY this exact JSON structure:
                {
                  "overallScore": <integer 0-10>,
                  "technicalAccuracy": <integer 0-10>,
                  "completeness": <integer 0-10>,
                  "communication": <integer 0-10>,
                  "strengths": ["specific strength 1", "specific strength 2"],
                  "missingPoints": ["missing point 1", "missing point 2"],
                  "improvements": ["improvement suggestion 1", "improvement suggestion 2"],
                  "betterAnswerStructure": "<brief description of how to structure a better answer>"
                }
                """;
    }

    private EvaluationResult parseResponse(String response) {
        String cleaned = cleanJson(response);
        try {
            EvaluationResult result = objectMapper.readValue(cleaned, EvaluationResult.class);
            clampScores(result);
            return result;
        } catch (AIServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse AI evaluation response. Cleaned response: {}", cleaned);
            throw new AIServiceException("Received an unexpected response format from AI. Please try again.");
        }
    }

    private void clampScores(EvaluationResult result) {
        if (result.getOverallScore() != null)
            result.setOverallScore(Math.max(0, Math.min(10, result.getOverallScore())));
        if (result.getTechnicalAccuracy() != null)
            result.setTechnicalAccuracy(Math.max(0, Math.min(10, result.getTechnicalAccuracy())));
        if (result.getCompleteness() != null)
            result.setCompleteness(Math.max(0, Math.min(10, result.getCompleteness())));
        if (result.getCommunication() != null)
            result.setCommunication(Math.max(0, Math.min(10, result.getCommunication())));
    }

    private String cleanJson(String response) {
        if (response == null) return "{}";
        return response
                .replaceAll("(?s)```json\\s*", "")
                .replaceAll("(?s)```\\s*", "")
                .trim();
    }
}
