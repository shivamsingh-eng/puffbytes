package com.puffbytes.puffbytes.authentication.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "google.client")
public class GoogleClientProperties {

    private String id;
    private String secret;
}
