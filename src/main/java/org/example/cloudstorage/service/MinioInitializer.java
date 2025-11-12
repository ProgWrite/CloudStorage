package org.example.cloudstorage.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.exception.MinioOperationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
@RequiredArgsConstructor
public class MinioInitializer {

    private final MinioClient minioClient;
    @Value("${MINIO_BUCKET_NAME}")
    private String bucketName;

    @PostConstruct
    public void init() {
        createBucket(bucketName);
    }

    private void createBucket(String bucketName) {
        try {
            boolean isBucketExists =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!isBucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (IOException | GeneralSecurityException | MinioException exception) {
            throw new MinioOperationException(
                    String.format("Failed to create bucket with name %s", bucketName)
            );
        }

    }

}
