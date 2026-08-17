package com.example.copilot.dto.job;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class JobDescriptionResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
}
