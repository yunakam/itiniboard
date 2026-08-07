package com.initiboard.api.service;

import com.initiboard.api.dto.BlockDetailResponse;
import com.initiboard.api.dto.CandidateBlockResponse;
import com.initiboard.api.dto.CreateBlockRequest;
import com.initiboard.api.entity.Activity;
import com.initiboard.api.entity.Block;
import com.initiboard.api.entity.Transfer;
import com.initiboard.api.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;
    private final ActivityRepository activityRepository;
    private final TransferRepository transferRepository;
    private final PlanRepository planRepository;
    private final BlockPositionRepository blockPositionRepository;
    private final TodoRepository todoRepository;

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

    @Transactional(readOnly = true)
    public List<CandidateBlockResponse> getCandidateBlocks(Long excludePlanId) {
        if (!planRepository.existsById(excludePlanId)) {
            throw new EntityNotFoundException(
                    "Plan not found: planId=" + excludePlanId
            );
        }

        return blockRepository.findCandidatesByExcludedPlanId(excludePlanId)
                .stream()
                .map(this::toCandidateBlockResponse)
                .toList();
    }

    private CandidateBlockResponse toCandidateBlockResponse(Block block) {
        long incompleteTodoCount = todoRepository.countIncompleteByBlockId(
                block.getBlockId()
        );

        long usedPlanCount = blockPositionRepository.countUsedPlansByBlockId(
                block.getBlockId()
        );

        return new CandidateBlockResponse(
                block.getBlockId(),
                block.getBlockType(),
                block.getBlockName(),
                createSummary(block),
                incompleteTodoCount,
                usedPlanCount
        );
    }

    private String createSummary(Block block) {
        if ("activity".equals(block.getBlockType())) {
            Activity activity = activityRepository.findById(block.getBlockId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Activity detail is missing for blockId="
                                    + block.getBlockId()
                    ));

            if (block.getBlockPlace() != null
                    && !block.getBlockPlace().isBlank()) {
                return block.getBlockPlace();
            }

            return activity.getActivityType();
        }

        if ("transfer".equals(block.getBlockType())) {
            Transfer transfer = transferRepository.findById(block.getBlockId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Transfer detail is missing for blockId="
                                    + block.getBlockId()
                    ));

            return transfer.getTransferDeparture()
                    + " → "
                    + transfer.getTransferArrival();
        }

        throw new IllegalStateException(
                "Invalid block type for blockId=" + block.getBlockId()
        );
    }
}
