package com.initiboard.api.dto;

import java.time.LocalDate;

public record PlanTodoResponse(
        Long todoId,
        Long blockId,
        String blockName,
        String todoContent,
        LocalDate todoDeadline,
        boolean isCompleted
) {
}
