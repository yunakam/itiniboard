package com.initiboard.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class CreateBlockRequest {

    @NotBlank(message = "ブロック種別を選んでください")
    @Pattern(
            regexp = "activity|transfer",
            message = "ブロック種別は activity または transfer を指定してください"
    )
    private String blockType;

    @NotBlank(message = "ブロック名を入力してください")
    @Size(max = 100, message = "ブロック名は100文字以内で入力してください")
    private String blockName;

    @Size(max = 255, message = "場所は255文字以内で入力してください")
    private String blockPlace;

    private String blockDetails;

    @Size(max = 30, message = "アクティビティタイプは30文字以内で入力してください")
    private String activityType;

    @Digits(integer = 10, fraction = 2, message = "費用は整数部10桁、小数部2桁以内で入力してください")
    @PositiveOrZero(message = "費用は0以上で入力してください")
    private BigDecimal activityCost;

    @PositiveOrZero(message = "所要時間は0以上で入力してください")
    @Max(value = 4294967295L, message = "所要時間が大きすぎます")
    private Integer activityDuration;

    @Size(max = 255, message = "出発地は255文字以内で入力してください")
    private String transferDeparture;

    @Size(max = 255, message = "到着地は255文字以内で入力してください")
    private String transferArrival;

    @Size(max = 30, message = "移動手段は30文字以内で入力してください")
    private String transferMethod;

    @Digits(integer = 10, fraction = 2, message = "費用は整数部10桁、小数部2桁以内で入力してください")
    @PositiveOrZero(message = "費用は0以上で入力してください")
    private BigDecimal transferCost;

    @PositiveOrZero(message = "所要時間は0以上で入力してください")
    @Max(value = 4294967295L, message = "入力値が大きすぎます")
    private Integer transferDuration;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime transferDepartureTime;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime transferArrivalTime;

    @AssertTrue(message = "アクティビティタイプを選んでください")
    public boolean isActivityTypeValid() {
        return !"activity".equals(blockType)
                || activityType != null && !activityType.isBlank();
    }

    @AssertTrue(message = "出発地を入力してください")
    public boolean isTransferDepartureValid() {
        return !"transfer".equals(blockType)
                || transferDeparture != null && !transferDeparture.isBlank();
    }

    @AssertTrue(message = "到着地を入力してください")
    public boolean isTransferArrivalValid() {
        return !"transfer".equals(blockType)
                || transferArrival != null && !transferArrival.isBlank();
    }
}