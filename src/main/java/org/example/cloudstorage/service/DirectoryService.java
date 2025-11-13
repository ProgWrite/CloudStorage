package org.example.cloudstorage.service;

import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.ResourceType;
import org.example.cloudstorage.dto.resourceResponseDto.FolderResponseDto;
import org.example.cloudstorage.dto.resourceResponseDto.ResourceResponseDto;
import org.example.cloudstorage.exception.InvalidPathException;
import org.example.cloudstorage.exception.MinioOperationException;
import org.example.cloudstorage.exception.ResourceExistsException;
import org.example.cloudstorage.exception.ResourceNotFoundException;
import org.example.cloudstorage.mapper.FileSystemItemMapper;
import org.springframework.stereotype.Service;
import utils.TraversalMode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.cloudstorage.validation.ValidationUtils.validateResourceName;
import static utils.PathUtils.*;


@Service
@RequiredArgsConstructor
public class DirectoryService {

    private final MinioClientService minioClientService;

    public void createRootDirectory(Long id) {
        minioClientService.putRootDirectory(id);
    }

    public FolderResponseDto createDirectory(Long id, String path) {
        if (!path.endsWith("/") || !isPathValid(path)) {
            throw new InvalidPathException("Invalid path.");
        }

        String parentPath = buildParentPath(path);

        if (!minioClientService.isPathExists(id, parentPath)) {
            throw new ResourceNotFoundException("Parent path not found.");
        }

        String folderName = extractResourceName(path, false);
        validateResourceName(folderName);

        if (isFolderExists(id, folderName, parentPath)) {
            throw new ResourceExistsException("Folder with this name already exists.");
        }

        minioClientService.putDirectory(id, path);
        return new FolderResponseDto(parentPath, folderName, ResourceType.DIRECTORY);
    }

    public List<ResourceResponseDto> getDirectory(Long id, String path, TraversalMode traversalMode) {
        if (!isPathValid(path)) {
            throw new InvalidPathException("Invalid path");
        }

        if (minioClientService.isPathExists(id, path)) {
            Iterable<Result<Item>> minioObjects = minioClientService.getListObjects(id, path, traversalMode);
            List<Item> items = extractAndFilterItemsFromMinio(minioObjects, id, path);
            return items.stream()
                    .map(item -> FileSystemItemMapper.INSTANCE.itemToDto(item, path))
                    .collect(Collectors.toList());
        }

        throw new ResourceNotFoundException("Folder with this name not found");
    }

    public boolean isFolderExists(Long id, String folderName, String parentPath) {
        List<ResourceResponseDto> files = getDirectory(id, parentPath, TraversalMode.NON_RECURSIVE);
        String folderNameWithSlash = folderName + "/";

        for (ResourceResponseDto file : files) {
            if (file.name().equals(folderNameWithSlash)) {
                return true;
            }
        }
        return false;
    }

    public List<Item> extractAndFilterItemsFromMinio(Iterable<Result<Item>> minioObjects, Long id, String path) {
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
            throw new MinioOperationException("Failed to extract items from MinIO for user with id " + id + "and path " + path);
        }
        return successfulItems;
    }

}
