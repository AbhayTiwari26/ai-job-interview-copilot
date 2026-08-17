package com.example.copilot.repository;

import com.example.copilot.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByAnalysisIdOrderByCreatedAtAsc(Long analysisId);

    @Query("SELECT i FROM Interview i JOIN i.analysis a JOIN a.resume r WHERE i.id = :id AND r.user.id = :userId")
    Optional<Interview> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT i FROM Interview i JOIN i.analysis a JOIN a.resume r WHERE a.id = :analysisId AND r.user.id = :userId ORDER BY i.createdAt ASC")
    List<Interview> findByAnalysisIdAndUserId(@Param("analysisId") Long analysisId, @Param("userId") Long userId);
}
