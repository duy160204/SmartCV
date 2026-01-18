package com.example.SmartCV.modules.admin.controller;

import com.example.SmartCV.modules.subscription.dto.CreatePlanRequest;
import com.example.SmartCV.modules.subscription.dto.PlanDefinitionDTO;
import com.example.SmartCV.modules.subscription.dto.UpdatePlanRequest;
import com.example.SmartCV.modules.subscription.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/plans")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPlanController {

    private final PlanService planService;

    @GetMapping
    public ResponseEntity<List<PlanDefinitionDTO>> listAllPlans() {
        return ResponseEntity.ok(planService.getAllPlans());
    }

    @PostMapping
    public ResponseEntity<PlanDefinitionDTO> createPlan(@jakarta.validation.Valid @RequestBody CreatePlanRequest req) {
        return ResponseEntity.ok(planService.createPlan(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanDefinitionDTO> updatePlan(@PathVariable Long id,
            @RequestBody @jakarta.validation.Valid UpdatePlanRequest req) {
        return ResponseEntity.ok(planService.updatePlan(id, req));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleStatus(@PathVariable Long id) {
        planService.togglePlanStatus(id);
        return ResponseEntity.ok().build();
    }
}
