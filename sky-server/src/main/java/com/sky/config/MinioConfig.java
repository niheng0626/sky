package com.sky.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MinioConfig {

    @Value("${sky.minio.endpoint}")
    private String endpoint;

    @Value("${sky.minio.accessKey}")
    private String accessKey;

    @Value("${sky.minio.secretKey}")
    private String secretKey;

    /**
     * 创建 MinioClient 客户端
     */
    @Bean
    public MinioClient minioClient() {
        try {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            log.info("MinIO客户端创建成功，Endpoint: {}", endpoint);
            return minioClient;

        } catch (Exception e) {
            log.error("MinIO客户端创建失败: {}", e.getMessage(), e);
            throw new RuntimeException("MinIO客户端初始化失败", e);
        }
    }
}