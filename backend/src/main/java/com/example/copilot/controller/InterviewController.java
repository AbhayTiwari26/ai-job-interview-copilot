package com.example.copilot.controller;

import com.example.copilot.dto.interview.AnswerRequest;
import com.example.copilot.dto.interview.InterviewGenerateRequest;
import com.example.copilot.dto.interview.InterviewResponse;
import com.example.copilot.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    /** Generate interview questions for a given analysis. Returns a list of Interview records (no answers yet). */
    @PostMapping("/generate")
    public ResponseEntity<List<InterviewResponse>> generateQuestions(
            @Valid @RequestBody InterviewGenerateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(interviewService.generateQuestions(request, userDetails.getUsername()));
    }

    /** Submit an answer for a specific interview question and receive AI evaluation. */
    @PostMapping("/{id}/answer")
    public ResponseEntity<InterviewResponse> submitAnswer(
            @PathVariable Long id,
            @Valid @RequestBody AnswerRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(interviewService.submitAnswer(id, request, userDetails.getUsername()));
    }

    /** Get all interview Q&A for a given analysis. */
    @GetMapping("/analysis/{analysisId}")
    public ResponseEntity<List<InterviewResponse>> getInterviewsByAnalysis(
            @PathVariable Long analysisId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(interviewService.getInterviewsByAnalysis(analysisId, userDetails.getUsername()));
    }
}
