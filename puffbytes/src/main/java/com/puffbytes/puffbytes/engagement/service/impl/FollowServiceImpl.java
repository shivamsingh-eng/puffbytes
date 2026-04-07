package com.puffbytes.puffbytes.engagement.service.impl;

import com.puffbytes.puffbytes.authentication.repository.UserRepository;
import com.puffbytes.puffbytes.common.exception.AlreadyFollowingException;
import com.puffbytes.puffbytes.common.exception.InvalidFollowException;
import com.puffbytes.puffbytes.engagement.entity.Follow;
import com.puffbytes.puffbytes.engagement.repository.FollowRepository;
import com.puffbytes.puffbytes.engagement.service.interfaces.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Override
    public void follow(String userId, String targetUserId) {

        // Self follow check
        if (userId.equals(targetUserId)) {
            throw new InvalidFollowException("You cannot follow yourself");
        }

        //if user does not exists
        if(!userRepository.existsByEmail(targetUserId)){
            throw new InvalidFollowException("You cannot follow user which does not exist");
        }

        // Already following check
        if (followRepository.existsByFollowerIdAndFollowingId(userId, targetUserId)) {
            throw new AlreadyFollowingException("Already following this user");
        }

        Follow follow = Follow.builder()
                .followerId(userId)
                .followingId(targetUserId)
                .createdAt(LocalDateTime.now())
                .build();

        followRepository.save(follow);
    }

    @Override
    @Transactional
    public void unfollow(String userId, String targetUserId) {
        userId = userId.trim().toLowerCase();
        targetUserId = targetUserId.trim().toLowerCase();

        boolean exists = followRepository.existsByFollowerIdAndFollowingId(userId, targetUserId);

        if (!exists) {
            throw new InvalidFollowException("You are not following this user");
        }

        followRepository.deleteByFollowerIdAndFollowingId(userId, targetUserId);
    }

    @Override
    public List<String> getFollowing(String userId) {

        return followRepository.findByFollowerId(userId)
                .stream()
                .map(Follow::getFollowingId)
                .toList();
    }
}