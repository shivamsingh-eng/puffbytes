package com.puffbytes.puffbytes.engagement.controller;

import com.puffbytes.puffbytes.common.util.SecurityUtil;
import com.puffbytes.puffbytes.upload.dto.ApiResponse;
import com.puffbytes.puffbytes.engagement.dto.CommentRequestDTO;
import com.puffbytes.puffbytes.engagement.dto.CommentResponseDTO;
import com.puffbytes.puffbytes.engagement.service.interfaces.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<String>> addComment(@PathVariable String postId, @Valid @RequestBody CommentRequestDTO request) {
        String userId = SecurityUtil.getCurrentUserId();

        commentService.addComment(postId, userId, request);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Comment added successfully")
                        .data(null)
                        .build()
        );
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<String>> deleteComment(@PathVariable Long commentId) {
        String userId = SecurityUtil.getCurrentUserId();

        commentService.deleteComment(commentId, userId);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Comment deleted successfully")
                        .data(null)
                        .build()
        );
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<Page<CommentResponseDTO>>> getComments(@PathVariable String postId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

        Page<CommentResponseDTO> response = commentService.getComments(postId, page, size);

        return ResponseEntity.ok(
                ApiResponse.<Page<CommentResponseDTO>>builder()
                        .success(true)
                        .message("Comments fetched successfully")
                        .data(response)
                        .build()
        );
    }
}