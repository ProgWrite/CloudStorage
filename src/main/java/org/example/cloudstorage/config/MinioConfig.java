package org.example.cloudstorage.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient(
            @Value("${MINIO_URL}")
            String url,
            @Value("${MINIO_USER}")
            String username,
            @Value("${MINIO_PASSWORD}")
            String password){
        return MinioClient.builder()
                .endpoint(url)
                .credentials(username,password)
                .build();
    }
}
