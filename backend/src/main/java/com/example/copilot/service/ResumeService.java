package com.example.copilot.service;

import com.example.copilot.dto.resume.ResumeResponse;
import com.example.copilot.entity.Resume;
import com.example.copilot.entity.User;
import com.example.copilot.exception.BadRequestException;
import com.example.copilot.exception.ResourceNotFoundException;
import com.example.copilot.repository.ResumeRepository;
import com.example.copilot.repository.UserRepository;
import com.example.copilot.util.PdfExtractorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final PdfExtractorUtil pdfExtractorUtil;

    public ResumeResponse uploadResume(MultipartFile file, String userEmail) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please upload a PDF file.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
            throw new BadRequestException("Only PDF files are accepted. Please upload a .pdf file.");
        }

        User user = getUserByEmail(userEmail);
        String extractedText = pdfExtractorUtil.extractText(file);

        Resume resume = Resume.builder()
                .user(user)
                .filename(file.getOriginalFilename())
                .extractedText(extractedText)
                .build();

        resume = resumeRepository.save(resume);
        log.info("Resume uploaded for user {}: {}", userEmail, file.getOriginalFilename());

        return toResponse(resume);
    }

    public List<ResumeResponse> getResumes(String userEmail) {
        User user = getUserByEmail(userEmail);
        return resumeRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ResumeResponse getResumeResponse(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);
        Resume resume = resumeRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found."));
        return toResponse(resume);
    }

    /** Returns the full entity (with extractedText) for internal use by other services. */
    public Resume getResumeEntity(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);
        return resumeRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found."));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private ResumeResponse toResponse(Resume resume) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .filename(resume.getFilename())
                .hasText(resume.getExtractedText() != null && !resume.getExtractedText().isBlank())
                .createdAt(resume.getCreatedAt())
                .build();
    }
}
