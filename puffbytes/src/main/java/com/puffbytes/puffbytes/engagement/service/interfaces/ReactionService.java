package com.puffbytes.puffbytes.engagement.service.interfaces;

import com.puffbytes.puffbytes.engagement.enums.ReactionType;

public interface ReactionService {
    void addReaction(String postId, String userId, ReactionType reactionType);
    void removeReaction(String postId, String userId);
}
