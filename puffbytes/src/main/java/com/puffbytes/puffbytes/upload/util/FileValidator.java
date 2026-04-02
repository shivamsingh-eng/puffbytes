package com.puffbytes.puffbytes.upload.util;

import com.puffbytes.puffbytes.common.exception.FileStorageException;
import com.puffbytes.puffbytes.common.exception.FileValidationException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class FileValidator {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    public static void validateImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileStorageException("File size exceeds 5MB limit");
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new FileValidationException("Invalid file type. Only JPEG, JPG and PNG allowed");
        }
    }
}