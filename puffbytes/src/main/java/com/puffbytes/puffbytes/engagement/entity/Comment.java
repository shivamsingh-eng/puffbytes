package com.puffbytes.puffbytes.engagement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private String postId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String commentText;

    @Column(name = "parent_comment_id")
    private Long parentCommentId; // for replies

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean isDeleted;
}