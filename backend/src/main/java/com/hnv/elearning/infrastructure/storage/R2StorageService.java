package com.hnv.elearning.infrastructure.storage;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class R2StorageService implements StorageService {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${r2.public-bucket}")   private String publicBucket;
    @Value("${r2.private-bucket}")  private String privateBucket;
    @Value("${r2.public-base-url}") private String publicBaseUrl;

    @Override
    public PresignedUploadResponse presignedUpload(BucketType bucketType, String key, String contentType, Duration ttl) {
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(getBucketName(bucketType))
                .key(key)
                .contentType(contentType)
                .build();

        //build presigned url, thông tin bucket, key lấy ở PutObjectRequest kiểu báo trước
        PresignedPutObjectRequest signed = s3Presigner.presignPutObject(rq ->
                rq.putObjectRequest(put)
                        .signatureDuration(ttl)
        );

        return new PresignedUploadResponse(signed.url().toString(), key);
    }


    @Override
    public String publicUrl(String key) {
        return publicBaseUrl + "/" + key;
    }

    @Override
    public boolean exist(BucketType bucketType, String key) {
        try{
            s3Client.headObject(builder -> builder.bucket(getBucketName(bucketType)).key(key));
            return true;
        }catch (NoSuchKeyException e){
            return false;
        }
    }

    @Override
    public void delete(BucketType bucketType, String key) {
        s3Client.deleteObject(builder -> builder.bucket(getBucketName(bucketType)).key(key));
    }

    private String getBucketName(BucketType bucketType) {
        return bucketType == BucketType.PUBLIC ? publicBucket : privateBucket;
    }
}
