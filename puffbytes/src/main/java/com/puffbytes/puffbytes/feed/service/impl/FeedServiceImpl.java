package com.puffbytes.puffbytes.feed.service.impl;

import com.puffbytes.puffbytes.engagement.service.interfaces.FollowService;
import com.puffbytes.puffbytes.feed.dto.FeedDTO;
import com.puffbytes.puffbytes.feed.service.interfaces.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final FollowService followService;

    @Override
    public List<FeedDTO> getFeed(String userId, int page, int size) {

        // STEP 1: Get following users
        // STEP 2: Fetch posts from upload service
        // STEP 3: Fetch engagement stats
        // STEP 4: Merge data
        // STEP 5: Apply ranking
        // STEP 6: Apply pagination


        // STEP 1: Get following users
        List<String> followingIds = followService.getFollowing(userId);

        // Include user's own posts
        followingIds.add(userId);

        // TEMP DEBUG
        System.out.println("Following users: " + followingIds);

        return List.of();
    }
}