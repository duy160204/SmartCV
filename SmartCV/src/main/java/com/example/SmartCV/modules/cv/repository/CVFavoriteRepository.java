package com.example.SmartCV.modules.cv.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SmartCV.modules.cv.domain.CVFavorite;

public interface CVFavoriteRepository extends JpaRepository<CVFavorite, Long> {

    List<CVFavorite> findByUserId(Long userId);

    Optional<CVFavorite> findByUserIdAndTemplateId(Long userId, Long templateId);

    boolean existsByUserIdAndTemplateId(Long userId, Long templateId);

    void deleteByUserIdAndTemplateId(Long userId, Long templateId);
}
