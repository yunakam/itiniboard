package com.initiboard.api.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

public record PlanPositionBlockResponse(
        String blockType,
        String blockName,
        String blockPlace,
        String blockDetails,
        BigDecimal blockCost,
        Integer blockDuration,

        String activityType,

        String transferDeparture,
        String transferArrival,
        String transferMethod,
        LocalTime transferDepartureTime,
        LocalTime transferArrivalTime,

        long incompleteTodoCount,
        long usedPlanCount
) {
}
