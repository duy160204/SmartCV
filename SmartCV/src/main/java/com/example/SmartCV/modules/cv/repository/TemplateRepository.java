package com.example.SmartCV.modules.cv.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SmartCV.modules.cv.domain.Template;
import com.example.SmartCV.modules.subscription.domain.PlanType;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    List<Template> findByIsActiveTrue();

    List<Template> findByIsActiveTrueAndPlanRequiredIn(List<PlanType> plans);

    List<Template> findByIsActiveTrueAndPlanRequired(PlanType planRequired);

    List<Template> findByIsActiveTrueOrderByCreatedAtDesc();

    long countByIsActiveTrue();
}
