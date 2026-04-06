package com.puffbytes.puffbytes.engagement.controller;

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

    //add reaction to the post
    @PostMapping("/{postId}/addreaction")
    public ResponseEntity<String> reactToPost(@PathVariable String postId, @Valid @RequestBody ReactionRequestDTO request, @RequestHeader("X-User-Id") String userId) {
        reactionService.addReaction(postId, userId, request.getReactionType());
        return ResponseEntity.ok("Reaction added successfully");
    }

    //remove reaction from a post
    @DeleteMapping("/{postId}/removereaction")
    public ResponseEntity<String> unreactPost(@PathVariable String postId, @RequestHeader("X-User-Id") String userId) {
        reactionService.removeReaction(postId, userId);
        return ResponseEntity.ok("Post unliked successfully");
    }
}