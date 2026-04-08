package com.puffbytes.puffbytes.authentication.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "github.client")
public class GithubClientProperties {

    private String id;
    private String secret;
}
