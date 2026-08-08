package com.initiboard.api.controller;

import com.initiboard.api.dto.*;
import com.initiboard.api.service.PlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @GetMapping
    public ResponseEntity<List<PlanResponse>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllPlans());
    }

    @GetMapping("/{planId}")
    public ResponseEntity<PlanResponse> getPlanById(
            @PathVariable Long planId
    ) {
        return ResponseEntity.ok(planService.getPlanById(planId));
    }

    @PostMapping
    public ResponseEntity<PlanResponse> createPlan(
            @Valid @RequestBody PlanRequest request
    ) {
        PlanResponse response = planService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{planId}")
    public ResponseEntity<PlanResponse> updatePlan(
            @PathVariable Long planId,
            @Valid @RequestBody PlanRequest request
    ) {
        return ResponseEntity.ok(planService.updatePlan(planId, request));
    }

    @PutMapping("/{planId}/positions")
    public ResponseEntity<List<PlanPositionResponse>> updatePlanPositions(
            @PathVariable Long planId,
            @Valid @RequestBody UpdatePlanPositionsRequest request
        ) {
        return ResponseEntity.ok(planService.updatePlanPositions(planId, request));
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<Void> deletePlan(
            @PathVariable Long planId
    ) {
        planService.deletePlan(planId);
        return ResponseEntity.ok().build();
    }
}