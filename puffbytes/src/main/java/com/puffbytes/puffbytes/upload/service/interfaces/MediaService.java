package com.puffbytes.puffbytes.upload.service.interfaces;

import com.puffbytes.puffbytes.upload.dto.MediaResponseDTO;
import com.puffbytes.puffbytes.upload.dto.MediaUploadResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MediaService {
    MediaUploadResponseDTO uploadImage(MultipartFile file, String userId);
    List<MediaResponseDTO> getUserMedia(String userId);
    void deleteMedia(String mediaId, String userId);
}
