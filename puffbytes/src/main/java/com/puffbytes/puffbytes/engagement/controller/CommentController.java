package com.puffbytes.puffbytes.engagement.controller;

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
    public ResponseEntity<String> addComment(@PathVariable String postId, @Valid @RequestBody CommentRequestDTO request, @RequestHeader("X-User-Id") String userId) {
        commentService.addComment(postId, userId, request);
        return ResponseEntity.ok("Comment added");
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId, @RequestHeader("X-User-Id") String userId) {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok("Comment deleted");
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Page<CommentResponseDTO>> getComments(@PathVariable String postId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(commentService.getComments(postId, page, size));
    }
}