package org.example.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.FileSystemItemResponseDto;
import org.example.cloudstorage.dto.ResourceType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final MinioClientService minioClientService;

    public List<FileSystemItemResponseDto> upload(Long id, String path, MultipartFile[] files){
       List<FileSystemItemResponseDto> directories = new ArrayList<>();

        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            minioClientService.putFile(id, path, file);
            directories.add(new FileSystemItemResponseDto(
                    path,
                    fileName,
                    file.getSize(),
                    ResourceType.FILE
                    )
            );
        }
        return directories;
    }

}
