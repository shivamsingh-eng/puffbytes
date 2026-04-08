package com.puffbytes.puffbytes;

import com.puffbytes.puffbytes.authentication.config.GithubClientProperties;
import com.puffbytes.puffbytes.authentication.config.GoogleClientProperties;
import com.puffbytes.puffbytes.authentication.config.JwtProperties;
import com.puffbytes.puffbytes.upload.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        JwtProperties.class,
        GithubClientProperties.class,
        GoogleClientProperties.class,
        StorageProperties.class
})
public class PuffbytesApplication {

	public static void main(String[] args) {
		SpringApplication.run(PuffbytesApplication.class, args);
	}

}
