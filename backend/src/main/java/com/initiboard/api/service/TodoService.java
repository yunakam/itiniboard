package com.initiboard.api.service;

import com.initiboard.api.dto.CreateTodoRequest;
import com.initiboard.api.dto.DeleteTodoResponse;
import com.initiboard.api.dto.TodoResponse;
import com.initiboard.api.dto.UpdateTodoRequest;
import com.initiboard.api.entity.Block;
import com.initiboard.api.entity.Todo;
import com.initiboard.api.repository.BlockRepository;
import com.initiboard.api.repository.TodoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final BlockRepository blockRepository;
    private final TodoRepository todoRepository;

    @Transactional
    public TodoResponse createTodo(
            Long blockId,
            CreateTodoRequest request
    ) {
        Block block = blockRepository.findById(blockId)
                .orElseThrow(() -> new EntityNotFoundException("Block not found: blockId=" + blockId));

        Todo todo = new Todo(
                block,
                request.getTodoContent(),
                request.getTodoDeadline()
        );

        Todo savedTodo = todoRepository.save(todo);

        return TodoResponse.from(savedTodo);
    }

    @Transactional
    public TodoResponse updateTodo(
            Long todoId,
            UpdateTodoRequest request
    ) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new EntityNotFoundException("Todo not found: todoId=" + todoId));

        todo.update(
                request.getTodoContent(),
                request.getTodoDeadline(),
                request.getIsCompleted()
        );

        return TodoResponse.from(todo);
    }

    @Transactional
    public DeleteTodoResponse deleteTodo(Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new EntityNotFoundException("Todo not found: todoId=" + todoId));

        todoRepository.delete(todo);

        return new DeleteTodoResponse(
                todoId,
                "TODOを削除しました"
        );
    }
}
