package com.initiboard.api.repository;

import com.initiboard.api.entity.BlockPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BlockPositionRepository
        extends JpaRepository<BlockPosition, Long> {

    // Count the plans for which a block is used
    @Query("""
            SELECT COUNT(DISTINCT bp.plan.planId)
            FROM BlockPosition bp
            WHERE bp.block.blockId = :blockId
            """)
    long countUsedPlansByBlockId(@Param("blockId") Long blockId);

    // Combine block_positions & plans and get plan_id and plan_name
    @Query(value = """
            SELECT DISTINCT
                p.plan_id,
                p.plan_name
            FROM block_positions bp
            INNER JOIN Plans p
                ON p.plan_id = bp.position_plan_id
            WHERE bp.position_block_id = :blockId
            ORDER BY p.plan_id
            """, nativeQuery = true)
    List<Object[]> findPlanUsageByBlockId(
            @Param("blockId") Long blockId
    );

    @Modifying
    @Query("""
        DELETE FROM  BlockPosition bp
        WHERE bp.plan.planId = :planId        
        """)
    void deleteAllByPlanId(@Param("planId") Long planId);
}