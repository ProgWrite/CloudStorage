package org.example.cloudstorage.service;

import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.FileSystemItemResponseDto;
import org.example.cloudstorage.dto.ResourceType;
import org.example.cloudstorage.exception.InvalidPathException;
import org.example.cloudstorage.mapper.FileSystemItemMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static utils.PathUtils.*;


@Service
@RequiredArgsConstructor
public class DirectoryService {

    private final MinioClientService minioClientService;

    public void createRootDirectory(Long id){
        minioClientService.putRootDirectory(id);
    }

    public FileSystemItemResponseDto createDirectory(Long id, String path){
        minioClientService.putDirectory(id, path);
        String truePath = buildPathForBackend(path);
        String folderName = extractFolderName(path, false);
        return new FileSystemItemResponseDto(truePath, folderName, null, ResourceType.DIRECTORY);
    }


    public List<FileSystemItemResponseDto>getDirectory(Long id, String path){
        Iterable<Result<Item>> minioObjects = minioClientService.getListObjects(id, path);
        List<Item> items = extractAndFilterItemsFromMinio(minioObjects, id, path);

        return items.stream()
                .map(item -> FileSystemItemMapper.INSTANCE.itemToDto(item,path))
                .collect(Collectors.toList());
    }


    //TODO подумай о кастомном исключении здесь!!!
    private List<Item> extractAndFilterItemsFromMinio(Iterable<Result<Item>> minioObjects, Long id, String path) {
        List<Item> successfulItems = new ArrayList<>();
        try{
            for(Result<Item> minioObject : minioObjects){
                Item item = minioObject.get();
                if(item.objectName().equals(buildRootPath(id) + path)){
                    continue;
                }
                successfulItems.add(item);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return successfulItems;
    }

}
