package com.puffbytes.puffbytes.upload.service.impl;

import com.puffbytes.puffbytes.upload.dto.MediaResponseDTO;
import com.puffbytes.puffbytes.upload.dto.MediaUploadResponseDTO;
import com.puffbytes.puffbytes.upload.entity.Media;
import com.puffbytes.puffbytes.upload.enums.MediaStatus;
import com.puffbytes.puffbytes.upload.enums.MediaType;
import com.puffbytes.puffbytes.common.exception.MediaNotFoundException;
import com.puffbytes.puffbytes.common.exception.UnauthorizedException;
import com.puffbytes.puffbytes.upload.repository.MediaRepository;
import com.puffbytes.puffbytes.upload.service.interfaces.FileStorageService;
import com.puffbytes.puffbytes.upload.service.interfaces.MediaService;
import com.puffbytes.puffbytes.upload.util.FileValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaServiceImpl.class);

    private final MediaRepository mediaRepository;
    private final ModelMapper modelMapper;
    private final FileStorageService fileStorageService;

    @Override
    public MediaUploadResponseDTO uploadImage(MultipartFile file, String userId) {

        FileValidator.validateImage(file); //validate file first, that it is image(jpg, jpeg or png)

        String FileURL = fileStorageService.uploadFile(file);

        Media media = Media.builder()
                .userId(userId)
                .mediaUrl(FileURL)
                .mediaType(MediaType.IMAGE)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .status(MediaStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Media saved = mediaRepository.save(media);
        log.debug("Saved media id={} userId={}", saved.getId(), saved.getUserId());

        // Using ModelMapper
        MediaUploadResponseDTO response = modelMapper.map(saved, MediaUploadResponseDTO.class);

        return response;
    }

    @Override
    public List<MediaResponseDTO> getUserMedia(String userId) {

        List<Media> mediaList = mediaRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, MediaStatus.ACTIVE);

        return mediaList.stream()
                .map(media -> modelMapper.map(media, MediaResponseDTO.class))
                .toList();
    }

    @Override
    public void deleteMedia(String mediaId, String userId) {

        Media media = mediaRepository
                .findByIdAndStatus(mediaId, MediaStatus.ACTIVE)
                .orElseThrow(() -> new MediaNotFoundException("Media not found"));

        //security check
        if (!userId.equals(media.getUserId())) {
            throw new UnauthorizedException("You are not authorized to delete this media");
        }

        media.setStatus(MediaStatus.INACTIVE);
        media.setUpdatedAt(LocalDateTime.now());

        mediaRepository.save(media);
    }
}