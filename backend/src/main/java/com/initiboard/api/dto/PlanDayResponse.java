package com.initiboard.api.dto;

import java.time.LocalDate;
import java.util.List;

public record PlanDayResponse(
        int dayNumber,
        LocalDate date,
        List<PlanPositionDetailResponse> positions
) {
}
