package com.fishtripplanner.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 정적 리소스를 외부 경로에서 서빙할 수 있도록 설정
        Path uploadDir = Paths.get("uploads");
        String uploadPath = uploadDir.toAbsolutePath().toString().replace("\\", "/");

        if (!uploadPath.endsWith("/")) {
            uploadPath += "/";
        }

        String resourceLocation = "file:" + uploadPath;
        System.out.println("📁 정적 리소스 매핑: /uploads/** → " + resourceLocation);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation)
                .setCachePeriod(0);
    }
}
