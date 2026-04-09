package com.puffbytes.puffbytes.upload.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedMediaDTO {

    private String id;
    private String userId;   //needed for feed
    private String mediaUrl;
    private String mediaType;
    private LocalDateTime createdAt;
}