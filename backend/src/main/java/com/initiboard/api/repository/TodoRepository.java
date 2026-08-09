package com.initiboard.api.repository;

import com.initiboard.api.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    // Count incomplete todos associated with a specified block
    @Query("""
            SELECT COUNT(t)
            FROM Todo t
            WHERE t.block.blockId = :blockId
              AND t.isCompleted = false
            """)
    long countIncompleteByBlockId(@Param("blockId") Long blockId);

    // Count incomplete todos by block
    @Query("""
        SELECT t.block.blockId, COUNT(t)
        FROM Todo t
        WHERE t.block.blockId IN :blockIds
        AND t.isCompleted = false
        GROUP BY t.block.blockId
    """)
    List<Object[]> countIncompleteByBlockIds(
            @Param("blockIds") Collection<Long> blockIds
    );
}