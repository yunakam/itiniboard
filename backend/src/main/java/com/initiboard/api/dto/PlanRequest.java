package com.initiboard.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PlanRequest {

    @NotBlank(message = "プラン名は必須です")
    private String planName;

    @NotNull(message = "開始日は必須です")
    private LocalDate planStartDate;

    @NotNull(message = "日数は必須です")
    @Min(value = 1, message = "日数は1以上である必要があります")
    private Integer dayCount;
}