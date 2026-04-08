package com.puffbytes.puffbytes.engagement.service.interfaces;

import java.util.List;

public interface FollowService {

    void follow(String userId, String targetUserId);

    void unfollow(String userId, String targetUserId);

    List<String> getFollowing(String userId);
}