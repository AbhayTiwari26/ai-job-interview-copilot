package com.example.copilot.controller;

import com.example.copilot.dto.analysis.AnalysisRequest;
import com.example.copilot.dto.analysis.AnalysisResponse;
import com.example.copilot.service.AnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping
    public ResponseEntity<AnalysisResponse> createAnalysis(
            @Valid @RequestBody AnalysisRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(analysisService.createAnalysis(request, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<AnalysisResponse>> getAnalyses(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(analysisService.getAnalyses(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnalysisResponse> getAnalysis(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(analysisService.getAnalysis(id, userDetails.getUsername()));
    }
}
