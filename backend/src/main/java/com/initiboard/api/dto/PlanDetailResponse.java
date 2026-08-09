package com.initiboard.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PlanDetailResponse(
        Long planId,
        String planName,
        LocalDate planStartDate,
        LocalDate planEndDate,
        int dayCount,
        BigDecimal totalCost,
        int totalTransferDuration,
        List<PlanDayResponse> days
) {
}
