package com.puffbytes.puffbytes.upload.entity;

import com.puffbytes.puffbytes.upload.enums.MediaStatus;
import com.puffbytes.puffbytes.upload.enums.MediaType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "media")
public class Media {

    @Id
    private String id;

    private String userId;

    private String mediaUrl;

    private MediaType mediaType; // IMAGE,VIDEO

    private String fileName;

    private Long fileSize;

    private String contentType;

    private MediaStatus status; // ACTIVE, INACTIVE

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}