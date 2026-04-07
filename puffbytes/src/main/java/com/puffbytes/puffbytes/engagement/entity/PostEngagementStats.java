package com.puffbytes.puffbytes.engagement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_engagement_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostEngagementStats {

    @Id
    @Column(name = "post_id")
    private String postId;

    private long reactionsCount;
    private long commentsCount;
    private long viewsCount;

    private LocalDateTime updatedAt;
}