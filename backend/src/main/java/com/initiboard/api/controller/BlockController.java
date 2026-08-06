package com.initiboard.api.controller;

import com.initiboard.api.dto.BlockDetailResponse;
import com.initiboard.api.dto.CreateBlockRequest;
import com.initiboard.api.service.BlockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
