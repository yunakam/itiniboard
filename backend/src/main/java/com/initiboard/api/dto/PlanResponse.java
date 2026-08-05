package com.initiboard.api.dto;

import com.initiboard.api.entity.Plan;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonPropertyOrder({
        "planId",
        "planName",
        "planStartDate",
        "planEndDate",
        "createdAt",
        "updatedAt"
})

@Getter
public class PlanResponse {

    private final Long planId;
    private final String planName;
    private final LocalDate planStartDate;
    private final LocalDate planEndDate;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public PlanResponse(Plan plan) {
        this.planId = plan.getPlanId();
        this.planName = plan.getPlanName();
        this.planStartDate = plan.getPlanStartDate();
        this.planEndDate = plan.getPlanEndDate();
        this.createdAt = plan.getCreatedAt();
        this.updatedAt = plan.getUpdatedAt();
    }
}