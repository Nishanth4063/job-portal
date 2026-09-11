package com.nishanth.jobportal.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get("uploads/resumes");
        String uploadPath = uploadDir.toFile().getAbsolutePath();
        
        // Maps the web request URL directly to the physical folder on your hard drive
        registry.addResourceHandler("/uploads/resumes/**")
                .addResourceLocations("file:/" + uploadPath + "/");
    }
}