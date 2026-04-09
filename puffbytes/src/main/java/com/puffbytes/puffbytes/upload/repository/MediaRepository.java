package com.puffbytes.puffbytes.upload.repository;

import com.puffbytes.puffbytes.upload.entity.Media;
import com.puffbytes.puffbytes.upload.enums.MediaStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MediaRepository extends MongoRepository<Media, String> {
    List<Media> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, MediaStatus status);
    Optional<Media> findByIdAndStatus(String id, MediaStatus status);
    List<Media> findByUserIdInAndStatusOrderByCreatedAtDesc(List<String> userIds, MediaStatus status);
}
