package com.example.copilot.controller;

import com.example.copilot.dto.resume.ResumeResponse;
import com.example.copilot.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping("/upload")
    public ResponseEntity<ResumeResponse> uploadResume(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resumeService.uploadResume(file, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<ResumeResponse>> getResumes(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(resumeService.getResumes(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> getResume(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(resumeService.getResumeResponse(id, userDetails.getUsername()));
    }
}
