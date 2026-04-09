package com.puffbytes.puffbytes.feed.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedDTO implements Serializable {

    private String postId;
    private String userId;
    private String imageUrl;

    private long reactionCount;
    private long commentCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // internal use (ranking)
    @JsonIgnore
    private double score;
}