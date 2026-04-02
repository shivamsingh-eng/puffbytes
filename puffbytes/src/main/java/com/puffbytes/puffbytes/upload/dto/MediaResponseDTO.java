package com.puffbytes.puffbytes.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MediaResponseDTO { //this response will be sent when we want to get particular post(through id)

    private String id;
    private String mediaUrl;
    private String mediaType;
    private LocalDateTime createdAt;
}