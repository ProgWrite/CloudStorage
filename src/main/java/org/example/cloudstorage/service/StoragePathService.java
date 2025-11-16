package org.example.cloudstorage.service;


import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.exception.InvalidPathException;
import org.example.cloudstorage.utils.PathUtils;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class StoragePathService {

    private static final String ROOT_PATH_TEMPLATE = "user-%d-files/";

    public String buildRootPath(Long userId) {
        if (userId == null) {
            throw new InvalidPathException("User ID cannot be null");
        }
        return String.format(ROOT_PATH_TEMPLATE, userId);
    }

    public String deleteRootPath(String fullPath, Long userId) {
        if (fullPath == null || userId == null) {
            throw new InvalidPathException("Path and User ID cannot be null");
        }
        String rootPath = buildRootPath(userId);
        return fullPath.replace(rootPath, "");
    }

    public String buildRelativePathFromMinioItem(Item item, String currentPath, Long userId) {
        if (item == null || currentPath == null || userId == null) {
            throw new InvalidPathException("Arguments cannot be null");
        }

        String fullPath = deleteRootPath(item.objectName(), userId);
        Path currentDirectory = Paths.get(currentPath);

        String relativePath = PathUtils.convertToRelativePath(fullPath, currentDirectory);

        if (fullPath.endsWith("/")) {
            return relativePath + "/";
        }
        return relativePath;
    }

}
