package com.initiboard.api.dto;

import com.initiboard.api.entity.Todo;

import java.time.LocalDate;

public record TodoResponse(
        Long todoId,
        String todoContent,
        LocalDate todoDeadline,
        boolean isCompleted
) {
    public static TodoResponse from(Todo todo) {
        return new TodoResponse(
                todo.getTodoId(),
                todo.getTodoContent(),
                todo.getTodoDeadline(),
                todo.isCompleted()
        );
    }
}
