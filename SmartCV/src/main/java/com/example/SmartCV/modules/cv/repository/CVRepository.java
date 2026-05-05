package com.example.SmartCV.modules.cv.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SmartCV.modules.cv.domain.CV;
import com.example.SmartCV.modules.cv.domain.CVStatus;

public interface CVRepository extends JpaRepository<CV, Long> {

    List<CV> findByUserId(Long userId);

    List<CV> findByUserIdAndStatus(Long userId, CVStatus status);

    boolean existsByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    List<CV> findByTemplateId(Long id);

    long countByIsPublicTrue();

    // PROJECTION – single query, LEFT JOIN to fetch template thumbnail
    @org.springframework.data.jpa.repository.Query(
        "SELECT new com.example.SmartCV.modules.cv.dto.projection.CVListProjection(" +
        "  c.id, c.title, c.templateId, c.status, c.isPublic, c.viewCount, c.createdAt, c.updatedAt," +
        "  t.thumbnailUrl" +
        ") FROM CV c LEFT JOIN Template t ON t.id = c.templateId " +
        "WHERE c.userId = :userId ORDER BY c.updatedAt DESC"
    )
    List<com.example.SmartCV.modules.cv.dto.projection.CVListProjection> findProjectionsByUserId(
            @org.springframework.data.repository.query.Param("userId") Long userId);
}
