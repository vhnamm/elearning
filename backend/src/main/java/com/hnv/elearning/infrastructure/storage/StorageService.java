package com.hnv.elearning.infrastructure.storage;

import java.time.Duration;

public interface StorageService {
    PresignedUploadResponse presignedUpload(BucketType bucketType, String key, String contentType, Duration ttl);

    String publicUrl(String key);
    boolean exist(BucketType bucketType,String key);
    void delete(BucketType bucketType,String key);
}
