package com.puffbytes.puffbytes.upload.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String uploadFile(MultipartFile file);
}