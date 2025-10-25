package org.example.cloudstorage.service;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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


    //TODO надо будет кастомное исключение
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
            throw new RuntimeException("Error creating RootFolder", exception);
        }
    }

    //TODO тут можно сделать общий метод (файлы + папки) в одном методе!!! И подумать о исключениях
    //TODO надо будет кастомное исключение
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
            throw new RuntimeException("Error creating Folder", exception);
        }
    }

    //TODO надо будет кастомное исключение (соединить с предыдущим методом).
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
            throw new RuntimeException("Error creating File", exception);
        }
    }

    //TODO почитать о PathReversal.
    public Iterable<Result<Item>> getListObjects(Long id, String path) {

        return minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(buildRootPath(id) + path)
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
      try{
          minioClient.removeObject(
                  RemoveObjectArgs.builder()
                          .bucket(bucketName)
                          .object(buildRootPath(id) + path)
                          .build());
      }catch (IOException | GeneralSecurityException | MinioException exception) {
          throw new RuntimeException("Error deleting File", exception);
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

}
