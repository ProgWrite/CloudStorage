package org.example.cloudstorage.validation;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.resourceResponseDto.ResourceResponseDto;
import org.example.cloudstorage.exception.InvalidPathException;
import org.example.cloudstorage.exception.ResourceExistsException;
import org.example.cloudstorage.exception.ResourceNotFoundException;
import org.example.cloudstorage.service.DirectoryService;
import org.example.cloudstorage.service.MinioClientService;
import org.springframework.stereotype.Component;
import utils.TraversalMode;

import java.util.List;

import static utils.PathUtils.*;

//TODO корректно ли тут ставить компонент. Мб переименовать класс
@RequiredArgsConstructor
@Component
public class ValidationUtils {

    private final MinioClientService minioClientService;
    private final DirectoryService directoryService;
    public final static int MIN_FOLDER_NAME_LENGTH = 3;
    public final static int MAX_FOLDER_NAME_LENGTH = 20;
    private static final int UPLOADED_RESOURCE_NAME_MAX_LENGTH = 50;

    public static void validateResourceName(String folderName) {
        if (folderName.length() < MIN_FOLDER_NAME_LENGTH || folderName.length() > MAX_FOLDER_NAME_LENGTH) {
            throw new InvalidPathException(String.format("Resource name must be between %d and %d characters",
                    MIN_FOLDER_NAME_LENGTH, MAX_FOLDER_NAME_LENGTH));
        }

    }

    public static void validateResourceNameForUpload(String folderName) {
        if (folderName.length() > UPLOADED_RESOURCE_NAME_MAX_LENGTH) {
            throw new InvalidPathException(String.format("Uploaded resource name must be less then %d characters",
                    UPLOADED_RESOURCE_NAME_MAX_LENGTH));
        }

    }

    public void validateMoveOperation(Long id, String currentPath, String newPath) {
        validatePathsFormat(currentPath, newPath);
        validatePathsExistence(id, currentPath, newPath);
        validateMoveRules(currentPath, newPath);
        validateResourceExistence(id, newPath);
    }

    private void validatePathsFormat(String currentPath, String newPath) {
        if (!isPathValidToMove(currentPath)) {
            throw new InvalidPathException("Invalid current path");
        }

        if (!isPathValidToMove(newPath)) {
            throw new InvalidPathException("Invalid new path");
        }

        validateResourceName(extractResourceName(newPath, false));

        if (!currentPath.endsWith("/")) {
            if (newPath.endsWith("/")) {
                throw new InvalidPathException("New path should end with resource name");
            }
        }

        if (currentPath.endsWith("/")) {
            if (!newPath.endsWith("/")) {
                throw new InvalidPathException("New path for folders should end with /");
            }
        }

    }

    private void validatePathsExistence(Long id, String currentPath, String newPath) {
        String parentCurrentPath = buildParentPath(currentPath);
        String parentNewPath = buildParentPath(newPath);

        if (!minioClientService.isPathExists(id, parentCurrentPath)) {
            throw new ResourceNotFoundException("Current path with this name not found");
        }

        if (!minioClientService.isPathExists(id, parentNewPath)) {
            throw new ResourceNotFoundException("New path with this name not found");
        }

        if (!isResourceExists(id, parentCurrentPath, currentPath)) {
            throw new ResourceNotFoundException("Resource with this name not found");
        }
    }

    private void validateMoveRules(String currentPath, String newPath){
        String parentCurrentPath = buildParentPath(currentPath);
        String parentNewPath = buildParentPath(newPath);

        if (!parentCurrentPath.equals(parentNewPath)) {
            String currentResourceName = extractResourceName(currentPath, false);
            String newResourceName = extractResourceName(newPath, false);

            if (!currentResourceName.equals(newResourceName)) {
                throw new InvalidPathException("Cannot change resource name during move operation");
            }
        }

        if (newPath.startsWith(currentPath) && newPath.length() > currentPath.length()
                && currentPath.endsWith("/") && newPath.endsWith("/")) {
            throw new InvalidPathException("Cannot move folder into its own subfolder");
        }
    }

    private void validateResourceExistence(Long id, String newPath){
        String parentNewPath = buildParentPath(newPath);

        if (isResourceExists(id, parentNewPath, newPath)) {
            throw new ResourceExistsException("Resource with this name already exists");
        }
    }

    private boolean isResourceExists(Long id, String parentPath, String path) {
        List<ResourceResponseDto> currentDirectory = directoryService.getDirectory(id, parentPath, TraversalMode.NON_RECURSIVE);
        boolean isTrailingSlash = checkTrailingSlash(parentPath, path);
        String resourceName = extractResourceName(path, isTrailingSlash);

        for (ResourceResponseDto dto : currentDirectory) {
            if (dto.name().equals(resourceName)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkTrailingSlash(String parentPath, String path) {
        if (path.endsWith("/")) {
            return true;
        }
        if (parentPath.equals("") && path.endsWith("/")) {
            return true;
        }
        return false;
    }

}
