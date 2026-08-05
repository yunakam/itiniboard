package com.initiboard.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Activity {

    @Id
    @Column(name = "block_id")
    private Long blockId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "block_id")
    private Block block;

    @Column(name = "activity_type", nullable = false, length = 30)
    private String activityType;

    @Column(name = "activity_cost", precision = 12, scale = 2)
    private BigDecimal activityCost;

    @Column(name = "activity_duration")
    private Long activityDuration;

    public Activity(
            Block block,
            String activityType,
            BigDecimal activityCost,
            Long activityDuration
    ) {
        this.block = block;
        this.activityType = activityType;
        this.activityCost = activityCost;
        this.activityDuration = activityDuration;
    }
}