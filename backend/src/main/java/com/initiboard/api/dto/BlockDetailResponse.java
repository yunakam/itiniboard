package com.initiboard.api.dto;

import com.initiboard.api.entity.Activity;
import com.initiboard.api.entity.Block;
import com.initiboard.api.entity.Transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record BlockDetailResponse(
        Long blockId,
        String blockType,
        String blockName,
        String blockPlace,
        String blockDetails,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

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

        List<TodoResponse> todos,
        List<BlockUsageResponse> usages
) {

    public static BlockDetailResponse fromActivity(
            Block block,
            Activity activity,
            List<TodoResponse> todos,
            List<BlockUsageResponse> usages
    ) {
        return new BlockDetailResponse(
                block.getBlockId(),
                block.getBlockType(),
                block.getBlockName(),
                block.getBlockPlace(),
                block.getBlockDetails(),
                block.getCreatedAt(),
                block.getUpdatedAt(),

                activity.getActivityType(),
                activity.getActivityCost(),
                activity.getActivityDuration(),

                null,
                null,
                null,
                null,
                null,
                null,
                null,

                todos,
                usages
        );
    }

    public static BlockDetailResponse fromTransfer(
            Block block,
            Transfer transfer,
            List<TodoResponse> todos,
            List<BlockUsageResponse> usages
    ) {
        return new BlockDetailResponse(
                block.getBlockId(),
                block.getBlockType(),
                block.getBlockName(),
                block.getBlockPlace(),
                block.getBlockDetails(),
                block.getCreatedAt(),
                block.getUpdatedAt(),

                null,
                null,
                null,

                transfer.getTransferDeparture(),
                transfer.getTransferArrival(),
                transfer.getTransferMethod(),
                transfer.getTransferCost(),
                transfer.getTransferDuration(),
                transfer.getTransferDepartureTime(),
                transfer.getTransferArrivalTime(),

                todos,
                usages
        );
    }
}