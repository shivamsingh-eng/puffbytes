package com.puffbytes.puffbytes.upload.service.impl;

import com.puffbytes.puffbytes.common.exception.FileStorageException;
import com.puffbytes.puffbytes.upload.service.interfaces.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
@Service
public class LocalFileStorageService implements FileStorageService {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

    @Override
    public String uploadFile(MultipartFile file) {

        try {
            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String originalName = file.getOriginalFilename();
            String extension = "";

            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID() + extension;

            File destination = new File(uploadDir + fileName);

            System.out.println("Saving file to: " + destination.getAbsolutePath()); // DEBUG

            file.transferTo(destination);

            return "http://localhost:8080/uploads/" + fileName;

        } catch (IOException e) {
            e.printStackTrace();
            throw new FileStorageException("Failed to upload file");
        }
    }
}