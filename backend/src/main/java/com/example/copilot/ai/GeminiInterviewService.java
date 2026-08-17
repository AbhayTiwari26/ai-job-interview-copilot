package com.example.copilot.ai;

import com.example.copilot.dto.interview.GeneratedQuestion;
import com.example.copilot.exception.AIServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiInterviewService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public List<GeneratedQuestion> generateQuestions(
            String resumeText,
            String jobTitle,
            String jobDescription,
            List<String> matchedSkills,
            List<String> missingSkills) {

        String prompt = buildPrompt(resumeText, jobTitle, jobDescription, matchedSkills, missingSkills);

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.debug("AI Interview questions raw response received.");
            return parseResponse(response);

        } catch (AIServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Gemini API call failed for interview generation: {}", e.getMessage());
            throw new AIServiceException("Interview question generation is temporarily unavailable. Please try again.");
        }
    }

    private String buildPrompt(String resumeText, String jobTitle, String jobDescription,
                               List<String> matchedSkills, List<String> missingSkills) {
        return """
                You are an expert interview coach. Generate personalized interview questions
                based on the candidate's specific resume and the job description.

                STRICT RULES:
                - Questions MUST reference specific details from the resume (projects, technologies, experiences).
                - Do NOT generate generic questions that could apply to any candidate.
                - Mix technical depth, project-based, and behavioral questions.
                - Questions about missing skills should be framed as learning/exploration questions.
                - Return ONLY valid JSON array. No markdown, no explanation.

                RESUME:
                """ + resumeText + """

                JOB TITLE: """ + jobTitle + """

                JOB DESCRIPTION:
                """ + jobDescription + """

                MATCHED SKILLS: """ + String.join(", ", matchedSkills) + """

                MISSING SKILLS: """ + String.join(", ", missingSkills) + """

                Return ONLY a JSON array of exactly 9 questions (3 TECHNICAL, 3 PROJECT, 3 BEHAVIORAL):
                [
                  {"question": "...", "category": "TECHNICAL", "difficulty": "MEDIUM"},
                  {"question": "...", "category": "TECHNICAL", "difficulty": "MEDIUM"},
                  {"question": "...", "category": "TECHNICAL", "difficulty": "HARD"},
                  {"question": "...", "category": "PROJECT", "difficulty": "MEDIUM"},
                  {"question": "...", "category": "PROJECT", "difficulty": "MEDIUM"},
                  {"question": "...", "category": "PROJECT", "difficulty": "HARD"},
                  {"question": "...", "category": "BEHAVIORAL", "difficulty": "EASY"},
                  {"question": "...", "category": "BEHAVIORAL", "difficulty": "MEDIUM"},
                  {"question": "...", "category": "BEHAVIORAL", "difficulty": "MEDIUM"}
                ]
                """;
    }

    private List<GeneratedQuestion> parseResponse(String response) {
        String cleaned = cleanJson(response);
        try {
            return objectMapper.readValue(cleaned,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, GeneratedQuestion.class));
        } catch (Exception e) {
            log.error("Failed to parse AI interview questions. Cleaned response: {}", cleaned);
            throw new AIServiceException("Received an unexpected response format from AI. Please try again.");
        }
    }

    private String cleanJson(String response) {
        if (response == null) return "[]";
        return response
                .replaceAll("(?s)```json\\s*", "")
                .replaceAll("(?s)```\\s*", "")
                .trim();
    }
}
