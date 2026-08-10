package com.initiboard.api.controller;

import com.initiboard.api.dto.CreateTodoRequest;
import com.initiboard.api.dto.DeleteTodoResponse;
import com.initiboard.api.dto.TodoResponse;
import com.initiboard.api.dto.UpdateTodoRequest;
import com.initiboard.api.service.TodoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @PostMapping("/blocks/{blockId}/todos")
    public ResponseEntity<TodoResponse> createTodo(
        @PathVariable @Positive Long blockId,
        @Valid @RequestBody CreateTodoRequest request
    ) {
        TodoResponse response = todoService.createTodo(blockId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/todos/{todoId}")
    public ResponseEntity<TodoResponse> updateTodo(
         @PathVariable @Positive Long todoId,
         @Valid @RequestBody UpdateTodoRequest request
    ) {
        return ResponseEntity.ok(
                todoService.updateTodo(todoId, request)
        );
    }

    @DeleteMapping("/todos/{todoId}")
    public ResponseEntity<DeleteTodoResponse> deleteTodo(
            @PathVariable @Positive Long todoId
    ) {
        return ResponseEntity.ok(
                todoService.deleteTodo(todoId)
        );
    }
}
