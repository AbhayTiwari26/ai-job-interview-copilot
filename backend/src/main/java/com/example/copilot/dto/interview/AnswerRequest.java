package com.example.copilot.dto.interview;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnswerRequest {

    @NotBlank(message = "Answer is required")
    private String answer;
}
