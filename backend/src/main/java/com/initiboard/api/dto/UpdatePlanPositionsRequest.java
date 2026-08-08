package com.initiboard.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UpdatePlanPositionsRequest {

    @NotNull(message = "配置情報を指定してください")
    @Valid
    private List<PlanPositionRequest> positions;
}
