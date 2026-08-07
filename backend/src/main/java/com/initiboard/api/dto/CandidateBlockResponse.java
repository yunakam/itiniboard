package com.initiboard.api.dto;

//List of blocks in the candidate area

public record CandidateBlockResponse(
        Long blockId,
        String blockType,
        String blockName,
        String summary,
            // activity: blockPlace if exists, else activityType
            // transfer: transferDeparture + " → " + transferArrival
        long incompleteTodoCount,
        long usedPlanCount
) {
}