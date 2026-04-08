package com.puffbytes.puffbytes.upload.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "puffbytes.storage")
public class StorageProperties {

    /**
     * Relative to user.dir if not absolute.
     */
    private String uploadDirectory = "uploads";

    /**
     * Base URL path returned in media URLs (no trailing slash).
     */
    private String publicBaseUrl = "http://localhost:8080/uploads";
}
