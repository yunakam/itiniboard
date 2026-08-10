package com.initiboard.api.repository;

import com.initiboard.api.entity.BlockPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlockPositionRepository
        extends JpaRepository<BlockPosition, Long> {

    // Count plans that use the specified block.
    @Query("""
            SELECT COUNT(DISTINCT bp.plan.planId)
            FROM BlockPosition bp
            WHERE bp.block.blockId = :blockId
            """)
    long countUsedPlansByBlockId(
            @Param("blockId") Long blockId
    );

    // Find plan IDs and names that use the specified block.
    @Query("""
            SELECT DISTINCT
                bp.plan.planId,
                bp.plan.planName
            FROM BlockPosition bp
            WHERE bp.block.blockId = :blockId
            ORDER BY bp.plan.planId
            """)
    List<Object[]> findPlanUsagesByBlockId(
            @Param("blockId") Long blockId
    );

    // Delete all block positions in the specified plan.
    @Modifying
    @Query("""
            DELETE FROM BlockPosition bp
            WHERE bp.plan.planId = :planId
            """)
    void deleteAllByPlanId(
            @Param("planId") Long planId
    );

    // Find the specified block position in the specified plan.
    @Query("""
            SELECT bp
            FROM BlockPosition bp
            WHERE bp.plan.planId = :planId
              AND bp.block.blockId = :blockId
            """)
    Optional<BlockPosition> findByPlanIdAndBlockId(
            @Param("planId") Long planId,
            @Param("blockId") Long blockId
    );

    // Find all positions on the specified day in a plan by position order.
    @Query("""
            SELECT bp
            FROM BlockPosition bp
            WHERE bp.plan.planId = :planId
              AND bp.positionDayNumber = :dayNumber
            ORDER BY bp.positionOrder
            """)
    List<BlockPosition> findByPlanIdAndDayNumberOrderByPositionOrder(
            @Param("planId") Long planId,
            @Param("dayNumber") Integer dayNumber
    );

    // Find all plans on all days in a plan by position order.
    @Query("""
            SELECT bp
            FROM BlockPosition bp
            JOIN FETCH bp.block
            WHERE bp.plan.planId = :planId
            ORDER BY bp.positionDayNumber, bp.positionOrder
            """)
    List<BlockPosition> findAllByPlanIdWithBlockOrderByDayAndOrder(
            @Param("planId") Long planId
    );
}