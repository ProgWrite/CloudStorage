package org.example.cloudstorage.service;

import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.FileSystemItemResponseDto;
import org.example.cloudstorage.dto.ResourceType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import utils.PathUtils;

import java.util.ArrayList;
import java.util.List;

import static utils.PathUtils.deleteRootPath;
import static utils.PathUtils.extractFolderName;

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

    //TODO возможно можно сделать общий buildDto для 2 методов
    public FileSystemItemResponseDto getResourceInfo(Long id, String path){
        StatObjectResponse object =  minioClientService.statObject(id, path);
        return buildDto(object, id);
    }



    private FileSystemItemResponseDto buildDto(StatObjectResponse object, Long id){
        String fullName = object.object();
        String relativePath = deleteRootPath(fullName, id);
        String truePath = PathUtils.buildPathForBackend(relativePath);

        String folderName = extractFolderName(fullName, false);

        return new FileSystemItemResponseDto(
                truePath,
                folderName,
                object.size(),
                ResourceType.FILE
        );
    }



}
