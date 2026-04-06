package com.puffbytes.puffbytes.engagement.service.impl;

import com.puffbytes.puffbytes.common.exception.CommentNotFoundException;
import com.puffbytes.puffbytes.common.exception.ParentCommentNotFoundException;
import com.puffbytes.puffbytes.common.exception.PostNotFoundException;
import com.puffbytes.puffbytes.engagement.dto.CommentRequestDTO;
import com.puffbytes.puffbytes.engagement.dto.CommentResponseDTO;
import com.puffbytes.puffbytes.engagement.entity.Comment;
import com.puffbytes.puffbytes.engagement.entity.PostEngagementStats;
import com.puffbytes.puffbytes.engagement.repository.CommentRepository;
import com.puffbytes.puffbytes.engagement.repository.PostEngagementStatsRepository;
import com.puffbytes.puffbytes.engagement.service.interfaces.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostEngagementStatsRepository statsRepository;

    @Override
    public void addComment(String postId, String userId, CommentRequestDTO request) {

        // Step 1: Validate parent comment (if reply)
        if (request.getParentCommentId() != null) {

            Comment parentComment = commentRepository
                    .findByIdAndIsDeletedFalse(request.getParentCommentId())
                    .orElseThrow(() -> new ParentCommentNotFoundException("Parent comment not found or deleted"));

            // OPTIONAL: ensure same post
            if (!parentComment.getPostId().equals(postId)) {
                throw new ParentCommentNotFoundException("Invalid parent comment for this post");
            }
        }

        // Step 2: Save comment
        Comment comment = Comment.builder()
                .postId(postId)
                .userId(userId)
                .commentText(request.getCommentText())
                .parentCommentId(request.getParentCommentId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        commentRepository.save(comment);

        // Step 3: update stats
        updateCommentCount(postId, true);
    }

    @Override
    public void deleteComment(Long commentId, String userId) {

        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found or already deleted"));

        // only owner can delete
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        comment.setDeleted(true);
        //comment.setCommentText("this comment has been deleted");
        comment.setUpdatedAt(LocalDateTime.now());

        commentRepository.save(comment);

        updateCommentCount(comment.getPostId(), false);
    }

    private void updateCommentCount(String postId, boolean increment) {

        PostEngagementStats stats = statsRepository
                .findById(postId)
                .orElse(PostEngagementStats.builder()
                        .postId(postId)
                        .reactionsCount(0)
                        .commentsCount(0)
                        .viewsCount(0)
                        .build());

        if (increment) {
            stats.setCommentsCount(stats.getCommentsCount() + 1);
        } else {
            stats.setCommentsCount(Math.max(0, stats.getCommentsCount() - 1));
        }

        stats.setUpdatedAt(LocalDateTime.now());

        statsRepository.save(stats);
    }

    @Override
    public Page<CommentResponseDTO> getComments(String postId, int page, int size) {

        boolean postExists = commentRepository.existsByPostId(postId) || statsRepository.existsById(postId);

        if (!postExists) {
            throw new PostNotFoundException("Post not found with id: " + postId);
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Comment> commentsPage = commentRepository.findByPostIdAndParentCommentIdIsNullAndIsDeletedFalse(postId, pageable);

        List<Comment> parentComments = commentsPage.getContent();

        // Step 1: collect all parent IDs
        List<Long> parentIds = parentComments.stream()
                .map(Comment::getId)
                .toList();

        // Step 2: fetch ALL replies in one query
        List<Comment> allReplies =
                commentRepository.findByParentCommentIdInAndIsDeletedFalse(parentIds);

        // Step 3: group replies by parentId
        Map<Long, List<Comment>> repliesMap =
                allReplies.stream().collect(Collectors.groupingBy(Comment::getParentCommentId));

        // Step 4: map response
        return commentsPage.map(comment -> {

            List<CommentResponseDTO> repliesDTO =
                    repliesMap.getOrDefault(comment.getId(), List.of())
                            .stream()
                            .map(reply -> CommentResponseDTO.builder()
                                    .id(reply.getId())
                                    .commentText(reply.getCommentText())
                                    .userId(reply.getUserId())
                                    .createdAt(reply.getCreatedAt())
                                    .build())
                            .toList();

            return CommentResponseDTO.builder()
                    .id(comment.getId())
                    .commentText(comment.getCommentText())
                    .userId(comment.getUserId())
                    .createdAt(comment.getCreatedAt())
                    .replies(repliesDTO)
                    .build();
        });
    }
}