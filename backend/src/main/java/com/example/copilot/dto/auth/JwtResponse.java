package com.example.copilot.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type;
    private Long id;
    private String name;
    private String email;
}
