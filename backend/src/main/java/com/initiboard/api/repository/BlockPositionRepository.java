package com.initiboard.api.repository;

import com.initiboard.api.entity.BlockPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlockPositionRepository
        extends JpaRepository<BlockPosition, Long> {

    // Add a method to count the plans for which a block is used
    @Query("""
            SELECT COUNT(DISTINCT bp.plan.planId)
            FROM BlockPosition bp
            WHERE bp.block.blockId = :blockId
            """)
    long countUsedPlansByBlockId(@Param("blockId") Long blockId);
}