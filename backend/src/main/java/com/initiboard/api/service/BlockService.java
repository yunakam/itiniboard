package com.initiboard.api.service;

import com.initiboard.api.dto.*;
import com.initiboard.api.entity.Activity;
import com.initiboard.api.entity.Block;
import com.initiboard.api.entity.Transfer;
import com.initiboard.api.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;
    private final ActivityRepository activityRepository;
    private final TransferRepository transferRepository;
    private final PlanRepository planRepository;
    private final BlockPositionRepository blockPositionRepository;
    private final TodoRepository todoRepository;
    private final BlockDeletionConfirmationService blockDeletionConfirmationService;

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

    @Transactional
    public BlockDetailResponse duplicateBlock (Long blockId) {
        Block sourceBlock = blockRepository.findById(blockId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Block not found: blockId=" + blockId
                ));

        Block duplicatedBlock = new Block(
                sourceBlock.getBlockType(),
                sourceBlock.getBlockName(),
                sourceBlock.getBlockPlace(),
                sourceBlock.getBlockDetails()
        );

        Block savedBlock = blockRepository.save(duplicatedBlock);

        if ("activity".equals(duplicatedBlock.getBlockType())) {
            Activity sourceActivity = activityRepository.findById(blockId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Activity detail is missing for blockId=" + blockId
                    ));

            Activity duplicatedActivity = new Activity(
                    savedBlock,
                    sourceActivity.getActivityType(),
                    sourceActivity.getActivityCost(),
                    sourceActivity.getActivityDuration()
            );

            Activity savedActivity = activityRepository.save(duplicatedActivity);

            return BlockDetailResponse.fromActivity(savedBlock, savedActivity);
        }

        if ("transfer".equals(sourceBlock.getBlockType())) {
            Transfer sourceTransfer = transferRepository.findById(blockId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Transfer detail is missing for blockId=" + blockId
                    ));

            Transfer duplicatedTransfer = new Transfer(
                    savedBlock,
                    sourceTransfer.getTransferDeparture(),
                    sourceTransfer.getTransferArrival(),
                    sourceTransfer.getTransferMethod(),
                    sourceTransfer.getTransferCost(),
                    sourceTransfer.getTransferDuration(),
                    sourceTransfer.getTransferDepartureTime(),
                    sourceTransfer.getTransferArrivalTime()
            );

            Transfer savedTransfer = transferRepository.save(duplicatedTransfer);

            return BlockDetailResponse.fromTransfer(savedBlock, savedTransfer);
        }

        throw new IllegalStateException(
                "Invalid block type for blockId=" + blockId
        );

    }

    @Transactional
    public BlockDetailResponse updateBlock(Long blockId, UpdateBlockRequest request) {
        Block block = blockRepository.findById(blockId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Block not found: blockId=" + blockId
        ));

        updateBlockBasicFields(block, request);

        if ("activity".equals(block.getBlockType())) {
            validateActivityUpdateRequest(request);

            Activity activity = activityRepository.findById(blockId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Activity detail is missing for blockId=" + blockId
                    ));

            updateActivityFields(activity, request);

            return BlockDetailResponse.fromActivity(block, activity);
        }

        if ("transfer".equals(block.getBlockType())) {
            validateTransferUpdateRequest(request);

            Transfer transfer = transferRepository.findById(blockId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Transfer detail is missing for blockId=" + blockId
                    ));

            updateTransferFields(transfer, request);

            return BlockDetailResponse.fromTransfer(block, transfer );
        }

        throw new IllegalStateException(
                "Invalid block type for blockId=" + blockId
        );
    }

    private void updateBlockBasicFields(Block block, UpdateBlockRequest request) {
        block.setBlockName(request.getBlockName());
        block.setBlockPlace(request.getBlockPlace());
        block.setBlockDetails(request.getBlockDetails());
    }

    public void validateActivityUpdateRequest(UpdateBlockRequest request) {
        if (request.getActivityType() == null || request.getActivityType().isBlank()) {
            throw new IllegalArgumentException("Invalid activity type");
        }

        if (hasTransferFields(request)) {
            throw new IllegalArgumentException("Transfer fields must NOT be set for an Activity block");
        }
    }

    public void validateTransferUpdateRequest(UpdateBlockRequest request) {
        if (request.getTransferDeparture() == null || request.getTransferDeparture().isBlank()) {
            throw new IllegalArgumentException("Transfer departure is required");
        }

        if (request.getTransferArrival() == null || request.getTransferArrival().isBlank()) {
            throw new IllegalArgumentException("Transfer arrival is required");
        }

        if (hasActivityFields(request)) {
            throw new IllegalArgumentException("Activity fields must NOT be set for a Transfer block");
        }
    }

    public boolean hasTransferFields(UpdateBlockRequest request) {
        return request.getTransferDeparture() != null
                || request.getTransferArrival() != null
                || request.getTransferMethod() != null
                || request.getTransferCost() != null
                || request.getTransferDuration() != null
                || request.getTransferDepartureTime() != null
                || request.getTransferArrivalTime() != null;
    }

    public boolean hasActivityFields(UpdateBlockRequest request) {
        return request.getActivityType() != null
                || request.getActivityCost() != null
                || request.getActivityDuration() != null;
    }

    public void updateActivityFields(Activity activity, UpdateBlockRequest request) {
        activity.setActivityType(request.getActivityType());
        activity.setActivityCost(request.getActivityCost());
        activity.setActivityDuration(request.getActivityDuration());
    }

    public void updateTransferFields(Transfer transfer, UpdateBlockRequest request) {
        transfer.setTransferDeparture(transfer.getTransferDeparture());
        transfer.setTransferArrival(transfer.getTransferArrival());
        transfer.setTransferMethod(transfer.getTransferMethod());
        transfer.setTransferCost(transfer.getTransferCost());
        transfer.setTransferDuration(transfer.getTransferDuration());
        transfer.setTransferDepartureTime(transfer.getTransferDepartureTime());
        transfer.setTransferArrivalTime(transfer.getTransferArrivalTime());
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

    @Transactional(readOnly = true)
    public BlockUsageResult getBlockUsage(Long blockId) {
        if (!blockRepository.existsById(blockId)) {
            throw new EntityNotFoundException(
                    "Block not found: blockId=" + blockId
            );
        }

        List<BlockUsageResponse> usages = blockPositionRepository
                .findPlanUsagesByBlockId(blockId)
                .stream()
                .map(row -> new BlockUsageResponse(
                        ((Number) row[0]).longValue(),
                        (String) row[1]
                ))
                .toList();

        Set<Long> usagePlanIds = usages.stream()
                .map(BlockUsageResponse::planId)
                .collect(Collectors.toUnmodifiableSet());

        String deletionConfirmationToken = blockDeletionConfirmationService.issueToken(
                blockId,
                usagePlanIds
        );

        return new BlockUsageResult(
                usages,
                deletionConfirmationToken
        );
    }

    @Transactional
    public DeleteBlockResponse deleteBlock(
            Long blockId,
            String deletionConfirmationToken) {

        Block block = blockRepository.findById(blockId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Block not found: blockId=" + blockId
                ));

        Set<Long> currentUsagePlanIds = blockPositionRepository
                .findPlanUsagesByBlockId(blockId)
                .stream()
                .map(row -> ((Number) row[0]).longValue())
                .collect(Collectors.toUnmodifiableSet());

        blockDeletionConfirmationService.verifyAndConsume(
                deletionConfirmationToken,
                blockId,
                currentUsagePlanIds
        );

        blockRepository.deleteById(blockId);
        blockRepository.flush();

        return new  DeleteBlockResponse(
                blockId,
                "ブロックを削除しました"
        );

    }

}
