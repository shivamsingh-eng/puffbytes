package com.puffbytes.puffbytes.engagement.repository;

import com.puffbytes.puffbytes.engagement.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByIdAndIsDeletedFalse(Long commentId);
    Page<Comment> findByPostIdAndParentCommentIdIsNullAndIsDeletedFalse(String postId, Pageable pageable);
    List<Comment> findByParentCommentIdAndIsDeletedFalse(Long parentId);
    List<Comment> findByParentCommentIdInAndIsDeletedFalse(List<Long> parentIds);
    boolean existsByPostId(String postId);
}