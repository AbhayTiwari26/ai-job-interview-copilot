package com.example.copilot.service;

import com.example.copilot.dto.job.JobDescriptionRequest;
import com.example.copilot.dto.job.JobDescriptionResponse;
import com.example.copilot.entity.JobDescription;
import com.example.copilot.entity.User;
import com.example.copilot.exception.ResourceNotFoundException;
import com.example.copilot.repository.JobDescriptionRepository;
import com.example.copilot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobDescriptionRepository jobRepository;
    private final UserRepository userRepository;

    public JobDescriptionResponse createJob(JobDescriptionRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        JobDescription job = JobDescription.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        return toResponse(jobRepository.save(job));
    }

    public List<JobDescriptionResponse> getJobs(String userEmail) {
        User user = getUserByEmail(userEmail);
        return jobRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public JobDescriptionResponse getJobResponse(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);
        JobDescription job = jobRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Job description not found."));
        return toResponse(job);
    }

    /** Returns the full entity for internal use by other services. */
    public JobDescription getJobEntity(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);
        return jobRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Job description not found."));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private JobDescriptionResponse toResponse(JobDescription job) {
        return JobDescriptionResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .createdAt(job.getCreatedAt())
                .build();
    }
}
