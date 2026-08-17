package com.initiboard.api.dto;

//List of blocks in the candidate area

import java.math.BigDecimal;

public record CandidateBlockResponse(
        Long blockId,
        String blockType,
        String blockName,
        String blockPlace,
        BigDecimal blockCost,
        Integer blockDuration,
        String activityType,
        String transferMethod,
        String transferDeparture,
        String transferArrival,
        long incompleteTodoCount,
        long usedPlanCount
) {
}