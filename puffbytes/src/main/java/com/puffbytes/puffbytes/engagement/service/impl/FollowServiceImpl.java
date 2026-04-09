package com.puffbytes.puffbytes.engagement.service.impl;

import com.puffbytes.puffbytes.authentication.repository.UserRepository;
import com.puffbytes.puffbytes.common.exception.AlreadyFollowingException;
import com.puffbytes.puffbytes.common.exception.InvalidFollowException;
import com.puffbytes.puffbytes.common.service.CacheService;
import com.puffbytes.puffbytes.engagement.entity.Follow;
import com.puffbytes.puffbytes.engagement.repository.FollowRepository;
import com.puffbytes.puffbytes.engagement.service.interfaces.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final CacheService cacheService;

    @Override
    public void follow(String userId, String targetUserId) {

        //Validate UUID format
        UUID targetUUID;
        try {
            targetUUID = UUID.fromString(targetUserId);
        } catch (Exception e) {
            throw new InvalidFollowException("Invalid userId format");
        }

        //Self follow check
        if (userId.equals(targetUserId)) {
            throw new InvalidFollowException("You cannot follow yourself");
        }

        //Check if target user exists
        if (!userRepository.existsById(targetUUID)) {
            throw new InvalidFollowException("User does not exist");
        }

        //Already following check
        if (followRepository.existsByFollowerIdAndFollowingId(userId, targetUserId)) {
            throw new AlreadyFollowingException("Already following this user");
        }

        //Save follow relation
        Follow follow = Follow.builder()
                .followerId(userId)
                .followingId(targetUserId)
                .createdAt(LocalDateTime.now())
                .build();

        followRepository.save(follow);

        /**
         *CACHE INVALIDATION
         * When user follows someone: Their feed changes (new posts should appear)
         */
        cacheService.evictFeedCache(userId);
    }

    @Override
    @Transactional
    public void unfollow(String userId, String targetUserId) {

        //Validate UUID
        try {
            UUID.fromString(targetUserId);
        } catch (Exception e) {
            throw new InvalidFollowException("Invalid userId format");
        }

        boolean exists = followRepository
                .existsByFollowerIdAndFollowingId(userId, targetUserId);

        if (!exists) {
            throw new InvalidFollowException("You are not following this user");
        }

        //Delete follow relation
        followRepository.deleteByFollowerIdAndFollowingId(userId, targetUserId);

        /**
         *CACHE INVALIDATION
         * When user unfollows: Their feed should remove that user's posts
         */
        cacheService.evictFeedCache(userId);
    }

    @Override
    public List<String> getFollowing(String userId) {

        return followRepository.findByFollowerId(userId)
                .stream()
                .map(Follow::getFollowingId)
                .toList();
    }
}