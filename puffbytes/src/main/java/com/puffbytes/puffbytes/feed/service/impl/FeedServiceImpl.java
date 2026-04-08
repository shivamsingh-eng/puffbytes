package com.puffbytes.puffbytes.feed.service.impl;

import com.puffbytes.puffbytes.engagement.entity.PostEngagementStats;
import com.puffbytes.puffbytes.engagement.service.interfaces.FollowService;
import com.puffbytes.puffbytes.engagement.service.interfaces.PostEngagementStatsService;
import com.puffbytes.puffbytes.feed.dto.FeedDTO;
import com.puffbytes.puffbytes.feed.service.interfaces.FeedService;
import com.puffbytes.puffbytes.upload.dto.FeedMediaDTO;
import com.puffbytes.puffbytes.upload.service.interfaces.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final FollowService followService;
    private final MediaService mediaService;
    private final PostEngagementStatsService postEngagementStatsService;

    @Override
    public List<FeedDTO> getFeed(String userId, int page, int size) {

        /**
         * STEP 1: Fetch all users that current user is following
         * This defines WHOSE posts will appear in the feed. We also include the current user's own posts.
         * Example:
         * user follows → [ronit, pruthvi]
         * final list → [ronit, pruthvi, currentUser]
         */
        List<String> followingIds = new ArrayList<>(followService.getFollowing(userId));
        followingIds.add(userId);


        /**
         * STEP 2: Fetch all media (posts) from MongoDB
         * We fetch posts of all users in followingIds using a single query.
         * This avoids N+1 problem (calling DB multiple times).
         * Internally: SELECT * FROM media WHERE userId IN (...) AND status = ACTIVE
         */
        List<FeedMediaDTO> mediaList = mediaService.getMediaByUserIds(followingIds);


         // Edge Case Handling: If no posts exist, return empty list immediately.
        if (mediaList.isEmpty()) {
            return List.of();
        }


        /**
         * STEP 3: Extract postIds from media
         * These postIds will be used to fetch engagement data in bulk.
         */
        List<String> postIds = mediaList.stream()
                .map(FeedMediaDTO::getId)
                .toList();


        /**
         * STEP 4: Fetch engagement stats (likes, comments) from PostgreSQL
         * We fetch all stats in ONE query using postIds.
         * This avoids N+1 queries.
         * Output: Map<postId, PostEngagementStats>
         */
        Map<String, PostEngagementStats> statsMap = postEngagementStatsService.getStatsByPostIds(postIds);


        /**
         * STEP 5: Merge Media + Engagement into FeedDTO
         * We combine:
         * - Media data (MongoDB)
         * - Engagement data (PostgreSQL)
         * If stats are missing -> default to 0
         */
        List<FeedDTO> feedList = new ArrayList<>(
                mediaList.stream()
                        .map(media -> {

                            PostEngagementStats stats = statsMap.get(media.getId());

                            long reactionCount = stats != null ? stats.getReactionsCount() : 0;
                            long commentCount = stats != null ? stats.getCommentsCount() : 0;

                            return FeedDTO.builder()
                                    .postId(media.getId())
                                    .userId(media.getUserId())
                                    .imageUrl(media.getMediaUrl())
                                    .reactionCount(reactionCount)
                                    .commentCount(commentCount)
                                    .createdAt(media.getCreatedAt())
                                    .build();
                        })
                        .toList()
        );


        /**
         * STEP 6: Apply Ranking Algorithm
         * We compute a score for each post based on: Engagement -> reactions & comments AND Recency -> newer posts should rank higher
         * Formula: score = (reactions * 2) + (comments * 3) + freshnessScore
         * freshnessScore = 1 / (hoursSincePost + 1)
         */
        feedList.forEach(feed -> {

            long reactions = feed.getReactionCount();
            long comments = feed.getCommentCount();

            long hoursSincePost = java.time.Duration
                    .between(feed.getCreatedAt(), java.time.LocalDateTime.now())
                    .toHours();

            double freshnessScore = 1.0 / (hoursSincePost + 1);

            double score = (reactions * 2) + (comments * 2.5) + freshnessScore;

            feed.setScore(score);
        });


        /**
         * STEP 7: Sort Feed by Score (Descending)
         * Highest scored posts appear first in the feed.
         */
        feedList.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));


        /**
         * STEP 8: Apply Pagination
         * We return only a subset of posts based on page & size.
         * page = 0, size = 10 -> first 10 posts
         */
        int start = page * size;
        int end = Math.min(start + size, feedList.size());

        if (start >= feedList.size()) {
            return List.of();
        }

        return feedList.subList(start, end);
    }
}