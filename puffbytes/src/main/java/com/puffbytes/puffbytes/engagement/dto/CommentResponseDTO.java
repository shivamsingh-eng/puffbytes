package com.puffbytes.puffbytes.engagement.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentResponseDTO {

    private Long id;
    private String commentText;
    private String userId;
    private LocalDateTime createdAt;
    private List<CommentResponseDTO> replies;
}