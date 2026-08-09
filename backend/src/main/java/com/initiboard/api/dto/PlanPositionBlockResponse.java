package com.initiboard.api.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

public record PlanPositionBlockResponse(
        String blockType,
        String blockName,
        String blockPlace,
        String blockDetails,

        String activityType,
        BigDecimal activityCost,
        Integer activityDuration,

        String transferDeparture,
        String transferArrival,
        String transferMethod,
        BigDecimal transferCost,
        Integer transferDuration,
        LocalTime transferDepartureTime,
        LocalTime transferArrivalTime,

        long incompleteTodoCount
) {
}
