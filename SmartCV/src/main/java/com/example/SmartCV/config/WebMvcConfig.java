package com.example.SmartCV.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.storage.preview-dir:./preview-storage/}")
    private String previewDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path previewPath = Paths.get(previewDir).toAbsolutePath().normalize();
        String previewAbsolutePath = "file:" + previewPath.toString() + "/";

        registry.addResourceHandler("/preview/**")
                .addResourceLocations(previewAbsolutePath)
                .setCachePeriod(31536000);
    }
}
