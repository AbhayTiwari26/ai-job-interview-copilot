package com.example.copilot.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Analysis analysis;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(nullable = false)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String answer;

    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String feedbackJson;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
