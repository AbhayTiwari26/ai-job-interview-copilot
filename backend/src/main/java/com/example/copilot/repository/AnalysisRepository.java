package com.example.copilot.repository;

import com.example.copilot.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    @Query("SELECT a FROM Analysis a JOIN a.resume r WHERE r.user.id = :userId ORDER BY a.createdAt DESC")
    List<Analysis> findByResumeUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT a FROM Analysis a JOIN a.resume r WHERE a.id = :id AND r.user.id = :userId")
    Optional<Analysis> findByIdAndResumeUserId(@Param("id") Long id, @Param("userId") Long userId);
}
