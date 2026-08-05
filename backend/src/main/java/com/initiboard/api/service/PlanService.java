package com.initiboard.api.service;

import com.initiboard.api.dto.PlanRequest;
import com.initiboard.api.dto.PlanResponse;
import com.initiboard.api.entity.Plan;
import com.initiboard.api.repository.PlanRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanService {

    private final PlanRepository planRepository;

    public List<PlanResponse> getAllPlans() {
        return planRepository.findAll()
                .stream()
                .map(PlanResponse::new)
                .toList();
    }

    public PlanResponse getPlanById(Long planId) {
        Plan plan = findPlanOrThrow(planId);
        return new PlanResponse(plan);
    }

    public PlanResponse createPlan(PlanRequest request) {
        LocalDate endDate = calculateEndDate(request.getPlanStartDate(), request.getDayCount());
        Plan plan = new Plan(request.getPlanName(), request.getPlanStartDate(), endDate);
        Plan saved = planRepository.save(plan);
        return new PlanResponse(saved);
    }

    public PlanResponse updatePlan(Long planId, PlanRequest request) {
        Plan plan = findPlanOrThrow(planId);
        LocalDate endDate = calculateEndDate(request.getPlanStartDate(), request.getDayCount());

        plan.setPlanName(request.getPlanName());
        plan.setPlanStartDate(request.getPlanStartDate());
        plan.setPlanEndDate(endDate);

        Plan updated = planRepository.saveAndFlush(plan);
        return new PlanResponse(updated);
    }

    public void deletePlan(Long planId) {
        Plan plan = findPlanOrThrow(planId);
        planRepository.delete(plan);
    }

    private Plan findPlanOrThrow(Long planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("プランが見つかりません: planId=" + planId));
    }

    private LocalDate calculateEndDate(LocalDate startDate, int dayCount) {
        return startDate.plusDays(dayCount - 1L);
    }
}