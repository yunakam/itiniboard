package com.initiboard.api.controller;

import com.initiboard.api.dto.*;
import com.initiboard.api.service.PlanService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@Validated
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @GetMapping
    public ResponseEntity<List<PlanResponse>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllPlans());
    }

    @GetMapping("/{planId}")
    public ResponseEntity<PlanDetailResponse> getPlanById(
            @PathVariable @Positive(message = "プランIDは1以上で指定してください")
            Long planId
    ) {
        PlanDetailResponse response = planService.getPlanById(planId);
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

    @DeleteMapping("/{planId}/blocks/{blockId}")
    public ResponseEntity<RemovePlanBlockResponse> removeBlockFromPlan(
            @PathVariable Long planId,
            @PathVariable Long blockId
    ) {
        return ResponseEntity.ok(
                planService.removeBlockFromPlan(planId, blockId)
        );
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<Void> deletePlan(
            @PathVariable Long planId
    ) {
        planService.deletePlan(planId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{planId}/todos")
    public ResponseEntity<List<PlanTodoResponse>> getPlanTodos(
            @PathVariable @Positive(message = "プランIDは1以上で指定してください")
            Long planId
    ) {
        return ResponseEntity.ok(planService.getPlanTodos(planId));
    }
}