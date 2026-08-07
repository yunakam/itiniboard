package com.initiboard.api.repository;

import com.initiboard.api.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BlockRepository extends JpaRepository<Block, Long> {

    // Get blocks that are not used in a specified plan
    @Query("""
            SELECT b
            FROM Block b
            WHERE NOT EXISTS (
                SELECT bp.positionId
                FROM BlockPosition bp
                WHERE bp.plan.planId = :planId
                  AND bp.block.blockId = b.blockId
            )
            ORDER BY b.blockId
            """)
    List<Block> findCandidatesByExcludedPlanId(
            @Param("planId") Long planId
    );
}