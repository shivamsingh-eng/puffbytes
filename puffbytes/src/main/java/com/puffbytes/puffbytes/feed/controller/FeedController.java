package com.puffbytes.puffbytes.feed.controller;

import com.puffbytes.puffbytes.common.dto.ApiResponse;
import com.puffbytes.puffbytes.common.util.SecurityUtil;
import com.puffbytes.puffbytes.feed.dto.FeedDTO;
import com.puffbytes.puffbytes.feed.service.interfaces.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FeedDTO>>> getFeed() {
        String userId = SecurityUtil.getCurrentUserId();
        List<FeedDTO> items = feedService.getFeedForUser(userId);
        return ResponseEntity.ok(
                ApiResponse.<List<FeedDTO>>builder()
                        .success(true)
                        .message("Feed retrieved successfully")
                        .data(items)
                        .build()
        );
    }
}
