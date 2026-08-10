package com.initiboard.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CreateTodoRequest {

    @NotBlank(message = "TODOを入力してください")
    @Size(max = 500, message = "TODOは500文字以内で入力してください")
    private String todoContent;

    private LocalDate todoDeadline;
}
