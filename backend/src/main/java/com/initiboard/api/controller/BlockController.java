package com.initiboard.api.controller;

import com.initiboard.api.dto.BlockDetailResponse;
import com.initiboard.api.dto.CandidateBlockResponse;
import com.initiboard.api.dto.CreateBlockRequest;
import com.initiboard.api.dto.UpdateBlockRequest;
import com.initiboard.api.service.BlockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
