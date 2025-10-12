package org.example.cloudstorage.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
@RequiredArgsConstructor
public class MinioClientService {
    private final MinioClient minioClient;
    @Value("${MINIO_BUCKET_NAME}")
    private String bucketName;

    //TODO возможно этот метод будет создавать не только корневые папки, но и обычные. Надо будет подумать.
    //TODO надо будет кастомное исключение
    //TODO магические переменные
    public void putRootFolder(Long id){
        try{
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(buildRootPath(id))
                            .stream(new ByteArrayInputStream(new byte[] {}), 0L, -1)
                            .contentType("application/octet-stream")
                            .build()
            );
        }catch (IOException | GeneralSecurityException | MinioException exception) {
            throw new RuntimeException("Error creating RootFolder", exception);
        }
    }

    private String buildRootPath(Long id){
        return "user-" + id + "-files/";
    }

}
