package com.initiboard.api.service;

import com.initiboard.api.dto.BlockDetailResponse;
import com.initiboard.api.dto.CreateBlockRequest;
import com.initiboard.api.entity.Activity;
import com.initiboard.api.entity.Block;
import com.initiboard.api.entity.Transfer;
import com.initiboard.api.repository.ActivityRepository;
import com.initiboard.api.repository.BlockRepository;
import com.initiboard.api.repository.TransferRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;
    private final ActivityRepository activityRepository;
    private final TransferRepository transferRepository;

    @Transactional
    public BlockDetailResponse createBlock(CreateBlockRequest request) {
        Block block = new Block(
                request.getBlockType(),
                request.getBlockName(),
                request.getBlockPlace(),
                request.getBlockDetails()
        );

        Block savedBlock = blockRepository.save(block);

        if ("activity".equals(request.getBlockType())) {
            Activity activity = new Activity(
                    savedBlock,
                    request.getActivityType(),
                    request.getActivityCost(),
                    request.getActivityDuration()
            );

            Activity savedActivity = activityRepository.save(activity);

            return BlockDetailResponse.fromActivity(savedBlock, savedActivity);
        }

        if ("transfer".equals(request.getBlockType())) {
            Transfer transfer = new Transfer(
                    savedBlock,
                    request.getTransferDeparture(),
                    request.getTransferArrival(),
                    request.getTransferMethod(),
                    request.getTransferCost(),
                    request.getTransferDuration(),
                    request.getTransferDepartureTime(),
                    request.getTransferArrivalTime()
            );

            Transfer savedTransfer = transferRepository.save(transfer);

            return BlockDetailResponse.fromTransfer(savedBlock, savedTransfer);
        }

        throw new IllegalArgumentException("Invalid block type");
    }

    @Transactional(readOnly = true)
    public BlockDetailResponse getBlock (Long blockId) {
        Block block = blockRepository.findById(blockId)
                .orElseThrow(() -> new EntityNotFoundException("Block not found: blockId=" + blockId));

        if ("activity".equals(block.getBlockType())) {
            Activity activity = activityRepository.findById(blockId)
                .orElseThrow(() -> new IllegalStateException("Activity detail is missing for blockId=" + blockId));

            return BlockDetailResponse.fromActivity(block, activity);

        }

        if ("transfer".equals(block.getBlockType())) {
            Transfer transfer = transferRepository.findById(blockId)
                .orElseThrow(() -> new IllegalStateException("Transfer detail is missing for blockId=" + blockId));

            return BlockDetailResponse.fromTransfer(block, transfer);

        }

        throw new IllegalStateException(
                "Invalid block type for blockId=" + blockId
        );
    }
}
