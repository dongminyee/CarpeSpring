package com.carpe.backend.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class R2Config {
    @Value("${cloud.r2.endpoint}")
    private String endpoint;

    @Value("${cloud.r2.access-key}")
    private String accessKey;

    @Value("${cloud.r2.secret-key}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint)) // 🌟 AWS가 아닌 R2 주소로 강제 덮어쓰기
                .region(Region.of("auto")) // R2는 리전 구분이 없으므로 'auto'로 설정
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}
