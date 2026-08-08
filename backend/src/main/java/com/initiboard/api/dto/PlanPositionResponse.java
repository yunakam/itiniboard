package com.initiboard.api.dto;

import com.initiboard.api.entity.BlockPosition;

public record PlanPositionResponse(
        Long positionId,
        Long blockId,
        Integer dayNumber,
        Integer positionOrder
) {
    public static PlanPositionResponse from(BlockPosition blockPosition) {
        return new PlanPositionResponse(
                blockPosition.getPositionId(),
                blockPosition.getBlock().getBlockId(),
                blockPosition.getPositionDayNumber(),
                blockPosition.getPositionOrder()
        );
    }
}
