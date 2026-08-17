package com.initiboard.api.service;

import com.initiboard.api.dto.*;
import com.initiboard.api.entity.*;
import com.initiboard.api.repository.*;
import com.initiboard.api.util.CopyNameGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanService {

    private final PlanRepository planRepository;
    private final BlockRepository blockRepository;
    private final BlockPositionRepository blockPositionRepository;

    private final ActivityRepository activityRepository;
    private final TransferRepository transferRepository;
    private final TodoRepository todoRepository;

    public List<PlanResponse> getAllPlans() {
        return planRepository.findAll()
                .stream()
                .map(this::toPlanResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlanDetailResponse getPlanById(Long planId) {
        Plan plan = findPlanOrThrow(planId);

        List<BlockPosition> positions =
                blockPositionRepository.findAllByPlanIdWithBlockOrderByDayAndOrder(
                        planId
                );

        List<Long> blockIds = positions.stream()
                .map(position -> position.getBlock().getBlockId())
                .distinct()
                .toList();

        Map<Long, Activity> activitiesByBlockId = activityRepository
                .findByBlockIdIn(blockIds)
                .stream()
                .collect(Collectors.toMap(
                        Activity::getBlockId,
                        Function.identity()
                ));

        Map<Long, Transfer> transfersByBlockId = transferRepository
                .findByBlockIdIn(blockIds)
                .stream()
                .collect(Collectors.toMap(
                        Transfer::getBlockId,
                        Function.identity()
                ));

        Map<Long, Long> incompleteTodoCounts = getIncompleteTodoCounts(blockIds);

        Map<Long, Long> usedPlanCounts = getUsedPlanCounts(blockIds);

        Map<Integer, List<PlanPositionDetailResponse>> positionByDay = positions
                .stream()
                .collect(Collectors.groupingBy(
                        BlockPosition::getPositionDayNumber,
                        LinkedHashMap::new,
                        Collectors.mapping(
                                position -> toPlanPositionDetailResponse(
                                        position,
                                        activitiesByBlockId,
                                        transfersByBlockId,
                                        incompleteTodoCounts,
                                        usedPlanCounts
                                ),
                                Collectors.toList()
                        )
                ));

        int dayCount = calculateDayCount(plan);

        List<PlanDayResponse> days = IntStream.rangeClosed(1, dayCount)
                .mapToObj(dayNumber -> new PlanDayResponse(
                        dayNumber,
                        plan.getPlanStartDate().plusDays(dayNumber - 1L),
                        positionByDay.getOrDefault(dayNumber, List.of())
                ))
                .toList();

        return new PlanDetailResponse(
                plan.getPlanId(),
                plan.getPlanName(),
                plan.getPlanStartDate(),
                plan.getPlanEndDate(),
                dayCount,
                calculateTotalCost(positions),
                calculateTotalTransferDuration(positions),
                days
        );
    }

    @Transactional(readOnly = true)
public List<PlanTodoResponse> getPlanTodos(Long planId) {
        findPlanOrThrow(planId);

        return todoRepository.findByPlanId(planId)
                .stream()
                .map(this::toPlanTodoResponse)
                .toList();
    }

    public PlanResponse createPlan(PlanRequest request) {
        LocalDate endDate = calculateEndDate(
                request.getPlanStartDate(),
                request.getDayCount()
        );

        Plan plan = new Plan(
                request.getPlanName(),
                request.getPlanStartDate(),
                endDate
        );

        Plan saved = planRepository.save(plan);

        return toPlanResponse(saved);
    }

    public PlanResponse updatePlan(Long planId, PlanRequest request) {
        Plan plan = findPlanOrThrow(planId);

        LocalDate endDate = calculateEndDate(
                request.getPlanStartDate(),
                request.getDayCount()
        );

        plan.setPlanName(request.getPlanName());
        plan.setPlanStartDate(request.getPlanStartDate());
        plan.setPlanEndDate(endDate);

        Plan updated = planRepository.saveAndFlush(plan);

        return toPlanResponse(updated);
    }


    @Transactional
    public PlanResponse duplicatePlan (Long planId) {
        Plan sourcePlan = findPlanOrThrow(planId);

        List<BlockPosition> sourcePositions = blockPositionRepository
                .findAllByPlanIdWithBlockOrderByDayAndOrder(planId);

        String duplicatePlanName = CopyNameGenerator.generate(
                sourcePlan.getPlanName(),
                planRepository::existsByPlanName
        );

        Plan duplicatedPlan = new Plan(
                duplicatePlanName,
                sourcePlan.getPlanStartDate(),
                sourcePlan.getPlanEndDate()
        );

        Plan savedPlan = planRepository.save(duplicatedPlan);

        List<BlockPosition> duplicatePositions = sourcePositions
                .stream()
                .map(sourcePosition -> new BlockPosition(
                        savedPlan,
                        sourcePosition.getBlock(),
                        sourcePosition.getPositionDayNumber(),
                        sourcePosition.getPositionOrder()
                )).toList();

        blockPositionRepository.saveAll(duplicatePositions);

        return toPlanResponse(savedPlan);
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

    private PlanResponse toPlanResponse(Plan plan) {
        List<BlockPosition> positions =
                blockPositionRepository.findAllByPlanIdWithBlockOrderByDayAndOrder(
                        plan.getPlanId()
                );

        if (positions.isEmpty()) {
            return new PlanResponse(
                    plan,
                    BigDecimal.ZERO,
                    calculateDayCount(plan)
            );
        }

        return new PlanResponse(
                plan,
                calculateTotalCost(positions),
                calculateDayCount(plan)
        );

    }

    private Map<Long, Long> getIncompleteTodoCounts(List<Long> blockIds) {
        /*  Repository の Object[] 集計結果を、blockId -> 未完TODO数 の Map に変換 */
        if (blockIds.isEmpty()) {
            return Map.of();
        }

        return todoRepository.countIncompleteByBlockIds(blockIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()
                ));

    }

    private Map<Long, Long> getUsedPlanCounts(List<Long> blockIds) {
        if (blockIds.isEmpty()) {
            return Map.of();
        }

        return blockPositionRepository.countUsedPlansByBlockIds(blockIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()
                ));
    }

    private PlanPositionDetailResponse toPlanPositionDetailResponse(
            /* BlockPosition, Block, Activity/Transfer, TODO 集計をフロントエンド用の1配置分 DTO に変換 */
            BlockPosition position,
            Map<Long, Activity> activitiesByBlockId,
            Map<Long, Transfer> transfersByBlockId,
            Map<Long, Long> incompleteTodoCounts,
            Map<Long, Long> usedPlanCounts
    ) {
        Block block = position.getBlock();
        Long blockId = block.getBlockId();

        Activity activity = activitiesByBlockId.get(blockId);
        Transfer transfer = transfersByBlockId.get(blockId);

        PlanPositionBlockResponse blockResponse =
                new PlanPositionBlockResponse(
                        block.getBlockType(),
                        block.getBlockName(),
                        block.getBlockPlace(),
                        block.getBlockDetails(),
                        block.getBlockCost(),
                        block.getBlockDuration(),

                        activity != null ? activity.getActivityType() : null,

                        transfer != null ? transfer.getTransferDeparture() : null,
                        transfer != null ? transfer.getTransferArrival() : null,
                        transfer != null ? transfer.getTransferMethod() : null,
                        transfer != null ? transfer.getTransferDepartureTime() : null,
                        transfer != null ? transfer.getTransferArrivalTime() : null,

                        incompleteTodoCounts.getOrDefault(blockId, 0L),
                        usedPlanCounts.getOrDefault(blockId, 0L)

                );

        return new PlanPositionDetailResponse(
                position.getPositionId(),
                blockId,
                position.getPositionDayNumber(),
                position.getPositionOrder(),
                blockResponse
        );
    }

    private BigDecimal calculateTotalCost(List<BlockPosition> positions) {
        return positions.stream()
                .map(BlockPosition::getBlock)
                .map(Block::getBlockCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int calculateTotalTransferDuration(
            List<BlockPosition> positions
    ) {
        return positions.stream()
                .map(BlockPosition::getBlock)
                .filter(block -> "transfer".equals(block.getBlockType()))
                .map(Block::getBlockDuration)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private PlanTodoResponse toPlanTodoResponse(Todo todo) {
        Block block = todo.getBlock();

        return new PlanTodoResponse(
                todo.getTodoId(),
                block.getBlockId(),
                block.getBlockName(),
                todo.getTodoContent(),
                todo.getTodoDeadline(),
                todo.isCompleted()
        );
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