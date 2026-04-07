package com.puffbytes.puffbytes.engagement.controller;

import com.puffbytes.puffbytes.common.util.SecurityUtil;
import com.puffbytes.puffbytes.upload.dto.ApiResponse;
import com.puffbytes.puffbytes.engagement.dto.ReactionRequestDTO;
import com.puffbytes.puffbytes.engagement.service.interfaces.ReactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;

    //Add / Update Reaction
    @PostMapping("/{postId}/reaction")
    public ResponseEntity<ApiResponse<String>> reactToPost(@PathVariable String postId, @Valid @RequestBody ReactionRequestDTO request) {
        String userId = SecurityUtil.getCurrentUserId();

        reactionService.addReaction(postId, userId, request.getReactionType());

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Reaction added successfully")
                        .data(null)
                        .build()
        );
    }

    // Remove Reaction
    @DeleteMapping("/{postId}/reaction")
    public ResponseEntity<ApiResponse<String>> removeReaction(@PathVariable String postId) {
        String userId = SecurityUtil.getCurrentUserId();

        reactionService.removeReaction(postId, userId);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Reaction removed successfully")
                        .data(null)
                        .build()
        );
    }
}