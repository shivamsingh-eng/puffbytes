package com.puffbytes.puffbytes.upload.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}

/*
    normally spring boot serves static files from /static, /resources, /public
    but our static files are stored in project-root/uploads
    so without this config http://localhost:8080/uploads/file.jpg  we will get 404 error
    so we tell spring whenever the request comes from /uploads/** serve files fron uploads/ folder
 */