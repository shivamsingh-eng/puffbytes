package com.puffbytes.puffbytes.upload.controller;

import com.puffbytes.puffbytes.upload.dto.ApiResponse;
import com.puffbytes.puffbytes.upload.dto.MediaResponseDTO;
import com.puffbytes.puffbytes.upload.dto.MediaUploadResponseDTO;
import com.puffbytes.puffbytes.upload.service.interfaces.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/media")
public class MediaController {
    private final MediaService mediaService;

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); //email stored in JWT
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<MediaUploadResponseDTO>> uploadImage(@RequestParam("file") MultipartFile file) {

        //JWT authenticated user
        String userId = getCurrentUserId();

        MediaUploadResponseDTO response = mediaService.uploadImage(file, userId);

        return ResponseEntity.ok(
                ApiResponse.<MediaUploadResponseDTO>builder()
                        .success(true)
                        .message("Upload successful")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<MediaResponseDTO>>> getUserMedia() {

        //jwt authenticated user
        String userId = getCurrentUserId();

        List<MediaResponseDTO> response = mediaService.getUserMedia(userId);

        return ResponseEntity.ok(
                ApiResponse.<List<MediaResponseDTO>>builder()
                        .success(true)
                        .message("User media fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<ApiResponse<String>> deleteMedia(@PathVariable String mediaId) {

        // JWT authenticated user
        String userId = getCurrentUserId();

        mediaService.deleteMedia(mediaId, userId);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Media deleted successfully")
                        .data(null)
                        .build()
        );
    }
}
