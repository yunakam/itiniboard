package com.initiboard.api.repository;

import com.initiboard.api.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    // Count the number of todos that are:
    //  - associated with the block, and
    //  - not completed
    @Query("""
            SELECT COUNT(t)
            FROM Todo t
            WHERE t.block.blockId = :blockId
              AND t.isCompleted = false
            """)
    long countIncompleteByBlockId(@Param("blockId") Long blockId);
}