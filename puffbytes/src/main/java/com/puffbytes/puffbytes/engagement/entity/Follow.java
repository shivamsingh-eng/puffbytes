package com.puffbytes.puffbytes.engagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "follows",
        uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "following_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who is following
    @Column(name = "follower_id", nullable = false)
    private String followerId;

    // Whom they are following
    @Column(name = "following_id", nullable = false)
    private String followingId;

    private LocalDateTime createdAt;
}