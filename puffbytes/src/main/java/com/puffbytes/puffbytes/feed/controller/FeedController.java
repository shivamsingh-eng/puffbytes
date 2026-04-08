package com.puffbytes.puffbytes.feed.controller;

import com.puffbytes.puffbytes.common.util.SecurityUtil;
import com.puffbytes.puffbytes.feed.dto.FeedDTO;
import com.puffbytes.puffbytes.feed.service.interfaces.FeedService;
import com.puffbytes.puffbytes.upload.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public ApiResponse<List<FeedDTO>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        String userId = SecurityUtil.getCurrentUserId();

        List<FeedDTO> feed = feedService.getFeed(userId, page, size);

        return ApiResponse.<List<FeedDTO>>builder()
                .success(true)
                .message("Feed fetched successfully")
                .data(feed)
                .build();
    }
}