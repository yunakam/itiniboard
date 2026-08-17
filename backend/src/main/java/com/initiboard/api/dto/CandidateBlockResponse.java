package com.initiboard.api.dto;

//List of blocks in the candidate area

public record CandidateBlockResponse(
        Long blockId,
        String blockType,
        String blockName,
        String blockPlace,
        String activityType,
        String transferMethod,
        String transferDeparture,
        String transferArrival,
        long incompleteTodoCount,
        long usedPlanCount
) {
}