package org.example.cloudstorage.service;

import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.FileSystemItemResponseDto;
import org.example.cloudstorage.dto.ResourceType;
import org.example.cloudstorage.exception.InvalidPathException;
import org.example.cloudstorage.exception.ResourceExistsException;
import org.example.cloudstorage.exception.ResourceNotFoundException;
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
    private final static long EMPTY_FOLDER_SIZE = 0L;

    public void createRootDirectory(Long id) {
        minioClientService.putRootDirectory(id);
    }

    public FileSystemItemResponseDto createDirectory(Long id, String path) {
        if (!path.endsWith("/") || !isPathValid(path)) {
            throw new InvalidPathException("Invalid path.");
        }

        String parentPath = buildParentPath(path);

        if (!minioClientService.isPathExists(id, parentPath)) {
            throw new ResourceNotFoundException("Parent path not found.");
        }

        String folderName = extractFolderName(path, false);

        if (isFolderExists(id, folderName, parentPath)) {
            throw new ResourceExistsException("Folder with this name already exists.");
        }

        minioClientService.putDirectory(id, path);
        return new FileSystemItemResponseDto(parentPath, folderName, EMPTY_FOLDER_SIZE, ResourceType.DIRECTORY);
    }


    public List<FileSystemItemResponseDto> getDirectory(Long id, String path) {
        if (!isPathValid(path)) {
            throw new InvalidPathException("Invalid path");
        }

        if (minioClientService.isPathExists(id, path)) {
            Iterable<Result<Item>> minioObjects = minioClientService.getListObjects(id, path);
            List<Item> items = extractAndFilterItemsFromMinio(minioObjects, id, path);
            return items.stream()
                    .map(item -> FileSystemItemMapper.INSTANCE.itemToDto(item, path))
                    .collect(Collectors.toList());
        }

        throw new ResourceNotFoundException("Folder with this name not found");
    }

    //TODO подумай о кастомном исключении здесь!!!
    private List<Item> extractAndFilterItemsFromMinio(Iterable<Result<Item>> minioObjects, Long id, String path) {
        List<Item> successfulItems = new ArrayList<>();
        try {
            for (Result<Item> minioObject : minioObjects) {
                Item item = minioObject.get();
                if (item.objectName().equals(buildRootPath(id) + path)) {
                    continue;
                }
                successfulItems.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return successfulItems;
    }

    public boolean isFolderExists(Long id, String folderName, String parentPath) {
        List<FileSystemItemResponseDto> files = getDirectory(id, parentPath);
        String folderNameWithSlash = folderName + "/";

        for (FileSystemItemResponseDto file : files) {
            if (file.name().equals(folderNameWithSlash)) {
                return true;
            }
        }
        return false;
    }

}
