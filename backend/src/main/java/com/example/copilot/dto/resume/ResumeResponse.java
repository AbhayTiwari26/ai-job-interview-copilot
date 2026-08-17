package com.example.copilot.dto.resume;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ResumeResponse {
    private Long id;
    private String filename;
    private boolean hasText;
    private LocalDateTime createdAt;
}
