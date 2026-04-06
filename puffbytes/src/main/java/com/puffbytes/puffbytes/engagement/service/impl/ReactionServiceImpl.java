package com.puffbytes.puffbytes.engagement.service.impl;

import com.puffbytes.puffbytes.common.exception.ReactionNotFoundException;
import com.puffbytes.puffbytes.engagement.entity.*;
import com.puffbytes.puffbytes.engagement.enums.ReactionType;
import com.puffbytes.puffbytes.engagement.repository.*;
import com.puffbytes.puffbytes.engagement.service.interfaces.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReactionServiceImpl implements ReactionService {

    private final ReactionRepository reactionRepository;
    private final PostEngagementStatsRepository statsRepository;

    @Override
    public void addReaction(String postId, String userId, ReactionType reactionType) {

        // 1. Check if already reacted
        Reaction existingReaction = reactionRepository
                .findByPostIdAndUserId(postId, userId)
                .orElse(null);

        if (existingReaction != null) {
            // Update reaction instead of throwing error
            existingReaction.setReactionType(reactionType);
            reactionRepository.save(existingReaction);
            return;
        }

        // 2. Save new reaction
        Reaction reaction = Reaction.builder()
                .postId(postId)
                .userId(userId)
                .reactionType(reactionType)
                .createdAt(LocalDateTime.now())
                .build();

        reactionRepository.save(reaction);

        // 3. Update stats ONLY if new reaction
        updateLikesCount(postId, true);
    }

    @Override
    public void removeReaction(String postId, String userId) {

        Reaction reaction = reactionRepository
                .findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new ReactionNotFoundException("Reaction not found"));

        reactionRepository.delete(reaction);

        // decrement engagement count
        updateLikesCount(postId, false);
    }

    //Internal helper method
    private void updateLikesCount(String postId, boolean increment) {

        PostEngagementStats stats = statsRepository
                .findById(postId)
                .orElse(PostEngagementStats.builder()
                        .postId(postId)
                        .reactionsCount(0)
                        .commentsCount(0)
                        .viewsCount(0)
                        .build());

        if (increment) {
            stats.setReactionsCount(stats.getReactionsCount() + 1);
        } else {
            stats.setReactionsCount(Math.max(0, stats.getReactionsCount() - 1));
        }

        stats.setUpdatedAt(LocalDateTime.now());

        statsRepository.save(stats);
    }
}