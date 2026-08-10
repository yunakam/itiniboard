package com.initiboard.api.dto;

public record DeleteTodoResponse(
        Long todoId,
        String message
) {
}
