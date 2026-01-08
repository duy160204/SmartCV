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
}
