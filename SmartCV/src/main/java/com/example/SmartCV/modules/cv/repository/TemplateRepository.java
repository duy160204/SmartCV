package com.example.SmartCV.modules.cv.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SmartCV.modules.cv.domain.Template;
import com.example.SmartCV.modules.cv.dto.TemplateSummaryProjection;
import com.example.SmartCV.modules.subscription.domain.PlanType;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    Page<Template> findByIsActiveTrue(Pageable pageable);

    Page<Template> findByIsActiveTrueAndPlanRequiredIn(List<PlanType> plans, Pageable pageable);

    Page<Template> findByIsActiveTrueAndPlanRequired(PlanType planRequired, Pageable pageable);

    Page<Template> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    long countByIsActiveTrue();
}
