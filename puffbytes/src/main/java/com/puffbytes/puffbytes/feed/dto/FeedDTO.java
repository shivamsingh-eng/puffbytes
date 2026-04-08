package com.puffbytes.puffbytes.feed.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedDTO {

    private String postId;
    private String userId;
    private String imageUrl;

    private long reactionCount;
    private long commentCount;

    private LocalDateTime createdAt;

    // internal use (ranking)
    @JsonIgnore
    private double score;
}