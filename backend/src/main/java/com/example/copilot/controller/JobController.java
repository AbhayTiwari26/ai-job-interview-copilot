package com.example.copilot.controller;

import com.example.copilot.dto.job.JobDescriptionRequest;
import com.example.copilot.dto.job.JobDescriptionResponse;
import com.example.copilot.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobDescriptionResponse> createJob(
            @Valid @RequestBody JobDescriptionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobService.createJob(request, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<JobDescriptionResponse>> getJobs(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(jobService.getJobs(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDescriptionResponse> getJob(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(jobService.getJobResponse(id, userDetails.getUsername()));
    }
}
