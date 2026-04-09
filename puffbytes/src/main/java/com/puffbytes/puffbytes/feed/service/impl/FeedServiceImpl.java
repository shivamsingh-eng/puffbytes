package com.puffbytes.puffbytes.feed.service.impl;

import com.puffbytes.puffbytes.engagement.entity.PostEngagementStats;
import com.puffbytes.puffbytes.engagement.service.interfaces.FollowService;
import com.puffbytes.puffbytes.engagement.service.interfaces.PostEngagementStatsService;
import com.puffbytes.puffbytes.feed.dto.FeedDTO;
import com.puffbytes.puffbytes.feed.service.interfaces.FeedService;
import com.puffbytes.puffbytes.upload.dto.FeedMediaDTO;
import com.puffbytes.puffbytes.upload.service.interfaces.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final FollowService followService;
    private final MediaService mediaService;
    private final PostEngagementStatsService postEngagementStatsService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<FeedDTO> getFeed(String userId, int page, int size) {

        /**
         * STEP 0: Create Cache Key
         */
        String cacheKey = "feed:" + userId + ":" + page + ":" + size;
        log.info("Checking cache for key: {}", cacheKey);

        /**
         * STEP 1: Check Redis Cache
         */
        Object cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            log.info("CACHE HIT for key: {}", cacheKey);
            return (List<FeedDTO>) cached;
        }

        log.info("CACHE MISS for key: {}", cacheKey);

        /**
         * STEP 2: Get following users + self
         */
        List<String> followingIds = new ArrayList<>(followService.getFollowing(userId));
        followingIds.add(userId);

        /**
         * STEP 3: Fetch media (MongoDB)
         */
        List<FeedMediaDTO> mediaList = mediaService.getMediaByUserIds(followingIds);

        if (mediaList.isEmpty()) {
            log.info("No posts found for user: {}", userId);
            return List.of();
        }

        /**
         * STEP 4: Extract postIds
         */
        List<String> postIds = mediaList.stream()
                .map(FeedMediaDTO::getId)
                .toList();

        /**
         * STEP 5: Fetch engagement stats (PostgreSQL)
         */
        Map<String, PostEngagementStats> statsMap =
                postEngagementStatsService.getStatsByPostIds(postIds);

        /**
         * STEP 6: Merge Media + Engagement
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
         * STEP 7: Ranking Algorithm
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
         * STEP 8: Sort by score
         */
        feedList.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        /**
         * STEP 9: Pagination
         */
        int start = page * size;
        int end = Math.min(start + size, feedList.size());

        if (start >= feedList.size()) {
            return List.of();
        }

        List<FeedDTO> result = feedList.subList(start, end);

        /**
         * STEP 10: Store in Redis (TTL = 5 minutes)
         */
        redisTemplate.opsForValue().set(cacheKey, result, Duration.ofMinutes(5));
        log.info("Stored feed in cache for key: {}", cacheKey);

        return result;
    }
}