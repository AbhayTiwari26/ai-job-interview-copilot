package com.example.copilot.dto.interview;

import lombok.Data;
import lombok.NoArgsConstructor;

/** Internal POJO representing one AI-generated interview question. */
@Data
@NoArgsConstructor
public class GeneratedQuestion {
    private String question;
    private String category;
    private String difficulty;
}
