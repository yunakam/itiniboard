package com.initiboard.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlanPositionRequest {

    @NotNull(message = "ブロックIDを指定してください")
    @Positive(message = "1以上を指定してください")
    private Long blockId;

    @NotNull(message = "〇日目を指定してください")
    @Positive(message = "1以上を指定してください")
    @Max(value = 4294967295L, message = "値が大きすぎます")
    private Integer dayNumber;

    @NotNull(message = "並び順を指定してください")
    @Positive(message = "1以上を指定してください")
    @Max(value = 4294967295L, message = "値が大きすぎます")
    private Integer positionOrder;

}
