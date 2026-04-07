package com.puffbytes.puffbytes.engagement.repository;

import com.puffbytes.puffbytes.engagement.entity.PostEngagementStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostEngagementStatsRepository extends JpaRepository<PostEngagementStats, String> {
    boolean existsById(String postId);
}