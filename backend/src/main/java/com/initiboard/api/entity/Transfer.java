package com.initiboard.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "transfers")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transfer {

    @Id
    @Column(name = "block_id")
    private Long blockId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "block_id")
    private Block block;

    @Column(name = "transfer_departure", nullable = false, length = 255)
    private String transferDeparture;

    @Column(name = "transfer_arrival", nullable = false, length = 255)
    private String transferArrival;

    @Column(name = "transfer_method", length = 30)
    private String transferMethod;

    @Column(name = "transfer_cost", precision = 12, scale = 2)
    private BigDecimal transferCost;

    @Column(name = "transfer_duration")
    private Integer transferDuration;

    @Column(name = "transfer_departure_time")
    private LocalTime transferDepartureTime;

    @Column(name = "transfer_arrival_time")
    private LocalTime transferArrivalTime;

    public Transfer(
            Block block,
            String transferDeparture,
            String transferArrival,
            String transferMethod,
            BigDecimal transferCost,
            Integer transferDuration,
            LocalTime transferDepartureTime,
            LocalTime transferArrivalTime
    ) {
        this.block = block;
        this.transferDeparture = transferDeparture;
        this.transferArrival = transferArrival;
        this.transferMethod = transferMethod;
        this.transferCost = transferCost;
        this.transferDuration = transferDuration;
        this.transferDepartureTime = transferDepartureTime;
        this.transferArrivalTime = transferArrivalTime;
    }
}