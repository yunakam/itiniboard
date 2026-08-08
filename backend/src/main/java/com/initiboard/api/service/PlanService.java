package com.initiboard.api.service;

import com.initiboard.api.dto.*;
import com.initiboard.api.entity.Block;
import com.initiboard.api.entity.BlockPosition;
import com.initiboard.api.entity.Plan;
import com.initiboard.api.repository.BlockPositionRepository;
import com.initiboard.api.repository.BlockRepository;
import com.initiboard.api.repository.PlanRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanService {

    private final PlanRepository planRepository;
    private final BlockRepository blockRepository;
    private final BlockPositionRepository blockPositionRepository;

    public List<PlanResponse> getAllPlans() {
        return planRepository.findAll()
                .stream()
                .map(PlanResponse::new)
                .toList();
    }

    public PlanResponse getPlanById(Long planId) {
        Plan plan = findPlanOrThrow(planId);
        return new PlanResponse(plan);
    }

    public PlanResponse createPlan(PlanRequest request) {
        LocalDate endDate = calculateEndDate(request.getPlanStartDate(), request.getDayCount());
        Plan plan = new Plan(request.getPlanName(), request.getPlanStartDate(), endDate);
        Plan saved = planRepository.save(plan);
        return new PlanResponse(saved);
    }

    public PlanResponse updatePlan(Long planId, PlanRequest request) {
        Plan plan = findPlanOrThrow(planId);
        LocalDate endDate = calculateEndDate(request.getPlanStartDate(), request.getDayCount());

        plan.setPlanName(request.getPlanName());
        plan.setPlanStartDate(request.getPlanStartDate());
        plan.setPlanEndDate(endDate);

        Plan updated = planRepository.saveAndFlush(plan);
        return new PlanResponse(updated);
    }

    public void deletePlan(Long planId) {
        Plan plan = findPlanOrThrow(planId);
        planRepository.delete(plan);
    }

    public RemovePlanBlockResponse removeBlockFromPlan(
            Long planId,
            Long blockId
    ) {
        findPlanOrThrow(planId);

        BlockPosition blockPosition = blockPositionRepository.findByPlanIdAndBlockId(planId, blockId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Block position not found: planId=" + planId + ", blockId=" + blockId));

        Integer positionDayNumber = blockPosition.getPositionDayNumber();

        blockPositionRepository.delete(blockPosition);
        blockPositionRepository.flush();

        resequencePositionOrders(planId, positionDayNumber);

        return new RemovePlanBlockResponse(
                planId,
                blockId,
                "ブロックをプランから外しました"
        );
    }

    public List<PlanPositionResponse> updatePlanPositions(
            Long planId,
            UpdatePlanPositionsRequest request
    ) {
        Plan plan = findPlanOrThrow(planId);

        validatePlanPositions(plan, request.getPositions());

        List<BlockPosition> newPositions = createBlockPositions(
                plan,
                request.getPositions()
        );

        blockPositionRepository.deleteAllByPlanId(planId);
        blockPositionRepository.flush();

        return blockPositionRepository.saveAll(newPositions)
                .stream()
                .sorted(
                        Comparator.comparing(BlockPosition::getPositionDayNumber)
                                .thenComparing(BlockPosition::getPositionOrder)
                )
                .map(PlanPositionResponse::from)
                .toList();
    }

    public List<BlockPosition> createBlockPositions(
            Plan plan,
            List<PlanPositionRequest> positionRequests
    ) {
        List<BlockPosition> blockPositions = new ArrayList<>();

        for (PlanPositionRequest positionRequest : positionRequests) {
            Block block = blockRepository.findById(positionRequest.getBlockId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Block not found: blockId=" + positionRequest.getBlockId()
                    ));

            blockPositions.add(new BlockPosition(
                    plan,
                    block,
                    positionRequest.getDayNumber(),
                    positionRequest.getPositionOrder()
            ));
        }

        return blockPositions;
    }

    private void validatePlanPositions(
            Plan plan,
            List<PlanPositionRequest> positionRequests
    ) {
        int dayCount = calculateDayCount(plan);

        Set<Long> blockIds = new HashSet<>();
        Set<String> dayAndOrders = new HashSet<>();
        Map<Integer, List<Integer>> ordersByDay = new HashMap<>();

        for (PlanPositionRequest positionRequest : positionRequests) {
            validateDayNumber(positionRequest, dayCount);

            if (!blockIds.add(positionRequest.getBlockId())) {
                throw new IllegalArgumentException(
                        "Duplicate block ID in plan positions"
                );
            }

            ordersByDay
                    .computeIfAbsent(
                            positionRequest.getDayNumber(),
                            ignored -> new ArrayList<>()
                    )
                    .add(positionRequest.getPositionOrder());
        }

        validateContinuousPositionOrders(ordersByDay);
    }

    private void validateDayNumber(
            PlanPositionRequest positionRequest,
            int dayCount
    ) {
        if (positionRequest.getDayNumber() > dayCount) {
            throw new IllegalArgumentException(
                    "Day number exceeds plan duration"
            );
        }
    }

    private void validateContinuousPositionOrders(
            Map<Integer, List<Integer>> ordersByDay
    ) {
        for (List<Integer> orders : ordersByDay.values()) {
            orders.sort(Integer::compareTo);

            for (int index = 0; index < orders.size(); index++) {
                int expectedOrder = index + 1;
                 if (orders.get(index) != expectedOrder) {
                     throw new IllegalArgumentException(
                             "Position order must be continuous from 1"
                     );
                 }
            }
        }
    }

    private void resequencePositionOrders(
            Long planId,
            Integer positionDayNumber
    ) {
        List<BlockPosition> remainingPosition = blockPositionRepository
                .findByPlanIdAndDayNumberOrderByPositionOrder(
                        planId,
                        positionDayNumber
                );

        for (int index = 0; index < remainingPosition.size(); index++) {
            BlockPosition position = remainingPosition.get(index);
            int expectedOrder = index + 1;

            if (position.getPositionOrder() != expectedOrder) {
                position.changePositionOrder(expectedOrder);
                blockPositionRepository.save(position);
            }
        }
    }

    private Plan findPlanOrThrow(Long planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Plan not found: planId=" + planId));
    }

    private int calculateDayCount(Plan plan) {
        return (int) ChronoUnit.DAYS.between(
                plan.getPlanStartDate(),
                plan.getPlanEndDate()
        ) + 1;
    }


    private LocalDate calculateEndDate(LocalDate startDate, int dayCount) {
        return startDate.plusDays(dayCount - 1L);
    }

}