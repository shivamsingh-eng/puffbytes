package com.puffbytes.puffbytes.engagement.repository;

import com.puffbytes.puffbytes.engagement.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFollowingId(String followerId, String followingId);

    void deleteByFollowerIdAndFollowingId(String followerId, String followingId);

    List<Follow> findByFollowerId(String followerId);
}