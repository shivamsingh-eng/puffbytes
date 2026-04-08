package com.puffbytes.puffbytes.authentication.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private long expiration;
    private final Refresh refresh = new Refresh();

    @Data
    public static class Refresh {
        private long expiration;
    }
}
