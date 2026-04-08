package com.puffbytes.puffbytes.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedDTO {

    private String postId;
    private String authorUserId;
    private String contentPreview;
    private List<String> mediaUrls;
    private LocalDateTime publishedAt;
}
