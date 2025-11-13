package org.example.cloudstorage.service;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.exception.MinioOperationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import utils.TraversalMode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Optional;

import static utils.PathUtils.buildRootPath;

@Service
@RequiredArgsConstructor
public class MinioClientService {
    private final MinioClient minioClient;
    private static final ByteArrayInputStream EMPTY_STREAM = new ByteArrayInputStream(new byte[]{});
    private static final Long EMPTY_FOLDER_SIZE = 0L;
    private static final int AUTO_PART_SIZE = -1;
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    @Value("${MINIO_BUCKET_NAME}")
    private String bucketName;

    public void putRootDirectory(Long id) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(buildRootPath(id))
                            .stream(EMPTY_STREAM, EMPTY_FOLDER_SIZE, AUTO_PART_SIZE)
                            .contentType(DEFAULT_CONTENT_TYPE)
                            .build()
            );
        } catch (IOException | GeneralSecurityException | MinioException exception) {
            throw new MinioOperationException(
                    String.format("Failed to create root directory '%s' for user %d in bucket '%s'",
                            buildRootPath(id), id, bucketName)
            );
        }
    }

    public void putDirectory(Long id, String path) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(buildRootPath(id) + path)
                            .stream(EMPTY_STREAM, EMPTY_FOLDER_SIZE, AUTO_PART_SIZE)
                            .build()
            );
        } catch (IOException | GeneralSecurityException | MinioException exception) {
            throw new MinioOperationException(
                    String.format("Failed to create directory in path: %s for user %d in bucket '%s'",
                            buildRootPath(id) + path, id, bucketName)
            );
        }
    }

    public void putFile(Long id, String path, MultipartFile file) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(buildRootPath(id) + path + file.getOriginalFilename())
                            .stream(file.getInputStream(), file.getSize(), AUTO_PART_SIZE)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (IOException | GeneralSecurityException | MinioException exception) {
            throw new MinioOperationException(
                    String.format("Failed to create file in path: %s for user %d in bucket '%s'",
                            buildRootPath(id) + path + file.getOriginalFilename(), id, bucketName)
            );
        }
    }

    public Iterable<Result<Item>> getListObjects(Long id, String path, TraversalMode traversalMode) {
        boolean searchType = (TraversalMode.RECURSIVE == traversalMode);

        return minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(buildRootPath(id) + path)
                .recursive(searchType)
                .build());
    }

    public Optional<StatObjectResponse> statObject(Long id, String path) {
        try {
            return Optional.of(minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(buildRootPath(id) + path)
                            .build()));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    public void removeObject(Long id, String path) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(buildRootPath(id) + path)
                            .build());
        } catch (IOException | GeneralSecurityException | MinioException exception) {
            throw new MinioOperationException(
                    String.format("Failed to delete object in path: %s for user %d in bucket '%s'",
                            buildRootPath(id), id, bucketName)
            );
        }

    }

    public InputStream getObject(Long id, String path) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(buildRootPath(id) + path)
                            .build()
            );
        } catch (Exception exception) {
            throw new MinioOperationException(
                    String.format("Failed to find object in path: %s for user %d in bucket '%s'",
                            buildRootPath(id), id, bucketName)
            );
        }
    }

    public boolean isPathExists(Long id, String path) {
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(buildRootPath(id) + path)
                .build()
        );
        return results.iterator().hasNext();
    }

    public void copyObject(Long id, String currentPath, String newPath) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(buildRootPath(id) + newPath)
                            .source(
                                    CopySource.builder()
                                            .bucket(bucketName)
                                            .object(buildRootPath(id) + currentPath)
                                            .build())
                            .build());
        } catch (Exception exception) {
            throw new MinioOperationException(
                    String.format("Failed to copy object from current path: %s to new path: %s for user %d in bucket '%s'",
                            currentPath, newPath, id, bucketName)
            );
        }

    }

}
