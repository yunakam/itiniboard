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
@Table(name = "blocks")
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "block_id")
    private Long blockId;

    @Column(name = "block_type", nullable = false, length = 100)
    private String blockType;

    @Column(name = "block_name", nullable = false, length = 100)
    private String blockName;

    @Column(name = "block_place", length = 255)
    private String blockPlace;

    @Column(name = "block_details")
    private String blockDetails;

    @Column(name = "block_cost", precision = 12, scale = 2)
    private BigDecimal blockCost;

    @Column(name = "block_duration")
    private Integer blockDuration;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Block(
            String blockType,
            String blockName,
            String blockPlace,
            String blockDetails,
            BigDecimal blockCost,
            Integer blockDuration
    ) {
        this.blockType = blockType;
        this.blockName = blockName;
        this.blockPlace = blockPlace;
        this.blockDetails = blockDetails;
        this.blockCost = blockCost;
        this.blockDuration = blockDuration;
    }
}