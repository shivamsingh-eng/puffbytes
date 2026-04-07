package com.puffbytes.puffbytes.engagement.service.interfaces;

import com.puffbytes.puffbytes.engagement.dto.CommentRequestDTO;
import com.puffbytes.puffbytes.engagement.dto.CommentResponseDTO;
import org.springframework.data.domain.Page;

public interface CommentService {
    void addComment(String postId, String userId, CommentRequestDTO request);
    void deleteComment(Long commentId, String userId);
    Page<CommentResponseDTO> getComments(String postId, int page, int size);
}
