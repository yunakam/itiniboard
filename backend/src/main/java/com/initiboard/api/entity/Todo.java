package com.initiboard.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "todos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_id")
    private Long todoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "todo_block_id", nullable = false)
    private Block block;

    @Column(name = "todo_content", nullable = false, length = 500)
    private String todoContent;

    @Column(name = "todo_deadline")
    private LocalDate todoDeadline;

    @Column(name = "is_completed", nullable = false, length = 10)
    private boolean isCompleted;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Todo(
            Block block,
            String todoContent,
            LocalDate todoDeadline
    ) {
        this.block = block;
        this.todoContent = todoContent;
        this.todoDeadline = todoDeadline;
        this.isCompleted = false;
    }
}