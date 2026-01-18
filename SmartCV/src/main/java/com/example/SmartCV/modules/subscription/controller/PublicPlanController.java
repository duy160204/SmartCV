package com.example.SmartCV.modules.subscription.controller;

import com.example.SmartCV.modules.subscription.dto.PlanDefinitionDTO;
import com.example.SmartCV.modules.subscription.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PublicPlanController {

    private final PlanService planService;

    @GetMapping
    public ResponseEntity<List<PlanDefinitionDTO>> listActivePlans() {
        return ResponseEntity.ok(planService.getActivePlans());
    }
}
