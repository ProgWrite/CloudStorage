package org.example.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioClientService minioClientService;

    public void createRootFolder(Long id){
        minioClientService.putRootFolder(id);
    }

}
