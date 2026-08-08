package com.initiboard.api.dto;

public record RemovePlanBlockResponse(
        Long planId,
        Long blockId,
        String message
) {
}
