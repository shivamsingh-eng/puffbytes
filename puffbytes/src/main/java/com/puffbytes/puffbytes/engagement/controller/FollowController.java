package com.puffbytes.puffbytes.engagement.controller;

import com.puffbytes.puffbytes.engagement.service.interfaces.FollowService;
import com.puffbytes.puffbytes.common.util.SecurityUtil;
import com.puffbytes.puffbytes.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/engagement/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{userId}")
    public ApiResponse<String> follow(@PathVariable String userId) {

        String currentUser = SecurityUtil.getCurrentUserId();

        followService.follow(currentUser, userId);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Successfully followed")
                .data(null)
                .build();
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<String> unfollow(@PathVariable String userId) {

        String currentUser = SecurityUtil.getCurrentUserId();

        followService.unfollow(currentUser, userId);

        return ApiResponse.<String>builder()
                .success(true)
                .message("unfollowed successfully")
                .data(null)
                .build();
    }

    @GetMapping("/following")
    public ApiResponse<List<String>> getFollowing() {

        String currentUser = SecurityUtil.getCurrentUserId();

        List<String> followingList = followService.getFollowing(currentUser);

        return ApiResponse.<List<String>>builder()
                .success(true)
                .message("Following list fetched successfully")
                .data(followingList)
                .build();
    }
}