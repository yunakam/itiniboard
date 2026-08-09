package com.initiboard.api.dto;

public record PlanPositionDetailResponse(
        Long positionId,
        Long blockId,
        int dayNumber,
        int positionOrder,
        PlanPositionBlockResponse block
) {
}
