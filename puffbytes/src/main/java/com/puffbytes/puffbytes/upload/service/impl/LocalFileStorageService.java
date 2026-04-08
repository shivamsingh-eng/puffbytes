package com.puffbytes.puffbytes.upload.service.impl;

import com.puffbytes.puffbytes.common.exception.FileStorageException;
import com.puffbytes.puffbytes.upload.config.StorageProperties;
import com.puffbytes.puffbytes.upload.service.interfaces.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalFileStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);

    private final StorageProperties storageProperties;

    @Override
    public String uploadFile(MultipartFile file) {

        try {
            Path uploadDir = resolveUploadDir();
            Files.createDirectories(uploadDir);

            String originalName = file.getOriginalFilename();
            String extension = "";

            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID() + extension;

            Path destination = uploadDir.resolve(fileName);
            log.debug("Saving file to {}", destination.toAbsolutePath());

            file.transferTo(destination.toFile());

            String base = storageProperties.getPublicBaseUrl();
            if (base.endsWith("/")) {
                base = base.substring(0, base.length() - 1);
            }
            return base + "/" + fileName;

        } catch (IOException e) {
            log.error("File upload failed", e);
            throw new FileStorageException("Failed to upload file");
        }
    }

    private Path resolveUploadDir() {
        Path p = Paths.get(storageProperties.getUploadDirectory());
        if (!p.isAbsolute()) {
            p = Paths.get(System.getProperty("user.dir")).resolve(p);
        }
        return p;
    }
}
