package com.initiboard.api.controller;

import com.initiboard.api.dto.*;
import com.initiboard.api.service.BlockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    @PostMapping
    public ResponseEntity<BlockDetailResponse> createBlock (
            @Valid @RequestBody CreateBlockRequest request
            ) {
        BlockDetailResponse response = blockService.createBlock(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{blockId}")
    public ResponseEntity<BlockDetailResponse> getBlock(
            @PathVariable Long blockId) {

        return ResponseEntity.ok(blockService.getBlock(blockId));

    }

    @GetMapping("/{blockId}/usages")
    public ResponseEntity<List<BlockUsageResponse>> getBlockUsages(
            @PathVariable Long blockId
    ) {
        return ResponseEntity.ok(blockService.getBlockUsage(blockId));
    }

    @PostMapping("{blockId}/duplicate")
    public ResponseEntity<BlockDetailResponse> duplicateBlock(
            @PathVariable Long blockId) {
        BlockDetailResponse response = blockService.duplicateBlock(blockId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{blockId}")
    public ResponseEntity<BlockDetailResponse> updateBlock(
            @PathVariable Long blockId,
            @Valid @RequestBody UpdateBlockRequest request
    ) {
        return ResponseEntity.ok(blockService.updateBlock(blockId, request));
    }

    @GetMapping
    public ResponseEntity<List<CandidateBlockResponse>> getCandidateBlocks(
            @RequestParam Long excludePlanId
    ) {
        return ResponseEntity.ok(blockService.getCandidateBlocks(excludePlanId)
        );
    }

}
