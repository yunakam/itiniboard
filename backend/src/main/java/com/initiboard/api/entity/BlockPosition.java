package com.initiboard.api.entity;

// Which block is located
// at which order
// of which day
// of which plans
// -> exclude blocks that already belong to a plan(s)

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "block_positions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "position_id")
    private Long positionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "position_plan_id", nullable = false)
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "position_block_id", nullable = false)
    private Block block;

    @Column(name = "position_day_number", nullable = false)
    private Integer positionDayNumber;

    @Column(name = "position_order", nullable = false)
    private Integer positionOrder;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public BlockPosition(
            Plan plan,
            Block block,
            Integer positionDayNumber,
            Integer positionOrder
    ) {
        this.plan = plan;
        this.block = block;
        this.positionDayNumber = positionDayNumber;
        this.positionOrder = positionOrder;
    }

    public void changePositionOrder(Integer positionOrder) {
        if (positionOrder == null || positionOrder < 0) {
            throw new IllegalArgumentException(
                    "Position order must be at least 1"
            );
        }

        this.positionOrder = positionOrder;
    }
}
