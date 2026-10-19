package com.hnv.elearning.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class R2Config {
    @Value("${r2.access-key}")
    private String accessKey;
    @Value("${r2.secret-key}")
    private String secretKey;
    @Value("${r2.endpoint}")
    private String endpoint;

    private URI endpointUri(){
        return URI.create(endpoint);
    }

    private StaticCredentialsProvider credentials(){
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));

    }

    private S3Configuration s3Configuration(){
        return S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();
    }

    @Bean
    public S3Client s3Client(){
        return S3Client.builder()
                .credentialsProvider(credentials())
                .endpointOverride(endpointUri())
                .region(Region.of("auto"))
                .serviceConfiguration(s3Configuration())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(endpointUri())
                .credentialsProvider(credentials())
                .region(Region.of("auto"))
                .serviceConfiguration(s3Configuration())
                .build();
    }
}
