package org.example.cloudstorage.service;

import io.minio.Result;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.IOUtils;
import org.example.cloudstorage.dto.FileSystemItemResponseDto;
import org.example.cloudstorage.dto.ResourceType;
import org.example.cloudstorage.exception.InvalidPathException;
import org.example.cloudstorage.exception.ResourceExistsException;
import org.example.cloudstorage.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import utils.PathUtils;
import utils.TraversalMode;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static utils.PathUtils.*;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final MinioClientService minioClientService;
    private final DirectoryService directoryService;
    private static final int BUFFER_SIZE_1KB = 1024;
    private static final int START_OF_BUFFER = 0;
    private static final int END_OF_INPUT_STREAM = -1;

    public FileSystemItemResponseDto getResourceInfo(Long id, String path) {
        String backendPath = buildParentPath(path);

        if (minioClientService.isPathExists(id, backendPath)) {
            return minioClientService.statObject(id, path)
                    .map(object -> buildDto(object, id))
                    .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        }

        throw new InvalidPathException("path does not exist");
    }

    public List<FileSystemItemResponseDto> upload(Long id, String path, MultipartFile[] files) {
        if (!isPathValid(path)) {
            throw new InvalidPathException("Invalid path");
        }
        if (isFileExists(files, path, id)) {
            throw new ResourceExistsException("File with this name already exists");
        }
        if (isResourceExists(id, path, files)) {
            throw new ResourceExistsException("Resource with this name already exists in this directory");
        }

        if (!minioClientService.isPathExists(id, path) && path.endsWith("/")) {
            return getUploadedFiles(files, id, path);
        }

        return getUploadedFiles(files, id, path);
    }

    public void delete(Long id, String path) {
        if (!isPathValidToDeleteOrDownload(path)) {
            throw new InvalidPathException("Invalid path");
        }

        if (path.endsWith("/")) {
            if (!minioClientService.isPathExists(id, path)) {
                throw new ResourceNotFoundException("Folder with this name not found");
            }
            deleteFolder(id, path);
        } else {
            if (!isFileExists(id, path)) {
                throw new ResourceNotFoundException("File with this name not found");
            }
            minioClientService.removeObject(id, path);
        }
    }

    public StreamingResponseBody download(Long id, String path) throws IOException {
        if (!isPathValidToDeleteOrDownload(path)) {
            throw new InvalidPathException("Invalid path");
        }

        if (path.endsWith("/")) {
            if (!minioClientService.isPathExists(id, path)) {
                throw new ResourceNotFoundException("Folder with this name not found");
            }
            return downloadFolder(id, path);
        }

        if (!isFileExists(id, path)) {
            throw new ResourceNotFoundException("File with this name not found");
        }
        return downloadFile(id, path);
    }

    // TODO подумай куда вынести всю валидацию (её здесь очень много)
    public FileSystemItemResponseDto move(Long id, String currentPath, String newPath) {
        if (!isPathValidToMove(currentPath)) {
            throw new InvalidPathException("Invalid current path");
        }

        if (!isPathValidToMove(newPath)) {
            throw new InvalidPathException("Invalid new path");
        }

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

        if (isResourceExists(id, parentNewPath, newPath)) {
            throw new ResourceExistsException("Resource with this name already exists");
        }

        return newPath.endsWith("/") ?
                moveFolder(currentPath, newPath, id) :
                moveFile(currentPath, newPath, id);
    }

    public List<FileSystemItemResponseDto> search(Long id, String query) {
        Iterable<Result<Item>> minioObjects = minioClientService.getListObjects(id, "", TraversalMode.RECURSIVE);
        List<Item> items = directoryService.extractAndFilterItemsFromMinio(minioObjects, id, "");

        return searchResources(items, id, query);
    }

    private FileSystemItemResponseDto buildDto(StatObjectResponse object, Long id) {

        String fullName = object.object();
        String relativePath = deleteRootPath(fullName, id);
        String truePath = PathUtils.buildParentPath(relativePath);

        String folderName = extractResourceName(fullName, false);

        return new FileSystemItemResponseDto(
                truePath,
                folderName,
                object.size(),
                ResourceType.FILE
        );
    }

    private boolean isFileExists(MultipartFile[] files, String path, Long id) {
        for (MultipartFile file : files) {
            String fullFilePath = path + file.getOriginalFilename();
            Optional<StatObjectResponse> existingFile = minioClientService.statObject(id, fullFilePath);

            if (existingFile.isPresent()) {
                return true;
            }
        }
        return false;
    }

    private boolean isFileExists(Long id, String path) {
        Optional<StatObjectResponse> existingFile = minioClientService.statObject(id, path);

        if (existingFile.isPresent()) {
            return true;
        }
        return false;
    }

    private List<FileSystemItemResponseDto> getUploadedFiles(MultipartFile[] files, Long id, String path) {
        List<FileSystemItemResponseDto> uploadedFiles = new ArrayList<>();
        Set<String> uniqueFolders = getUniqueFolders(files, path, id);

        if (!uniqueFolders.isEmpty()) {
            for (String folderName : uniqueFolders) {
                minioClientService.putDirectory(id, folderName);
            }
        }

        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            minioClientService.putFile(id, path, file);
            uploadedFiles.add(new FileSystemItemResponseDto(
                            path,
                            fileName,
                            file.getSize(),
                            ResourceType.FILE
                    )
            );
        }
        return uploadedFiles;
    }


    private void deleteFolder(Long id, String path) {
        List<FileSystemItemResponseDto> files = directoryService.getDirectory(id, path, TraversalMode.NON_RECURSIVE);
        for (FileSystemItemResponseDto file : files) {
            String pathForDelete = path + file.name();
            if (pathForDelete.endsWith("/")) {
                deleteFolder(id, pathForDelete);
            } else {
                minioClientService.removeObject(id, pathForDelete);
            }
        }
        minioClientService.removeObject(id, path);
    }

    private Set<String> getUniqueFolders(MultipartFile[] files, String path, Long id) {
        Set<String> uniqueFolders = new HashSet<>();

        for (MultipartFile file : files) {
            String resourceName = path + file.getOriginalFilename();
            for (int i = 0; i < resourceName.length(); i++) {
                if (resourceName.charAt(i) == '/') {

                    if (minioClientService.isPathExists(id, resourceName.substring(0, i + 1))) {
                        continue;
                    }

                    uniqueFolders.add(resourceName.substring(0, i + 1));
                }
            }
        }

        return uniqueFolders;
    }

    //TODO подумай как избавиться от дублирования кода здесь (с методом выше)
    private Set<String> getUniqueFolders(List<Item> items, String newPath) {
        Set<String> uniqueFolders = new HashSet<>();

        for (Item item : items) {
            String resourceName = extractResourceName(item.objectName(), false);
            String fullPath = newPath + resourceName;
            for (int i = 0; i < fullPath.length(); i++) {
                if (fullPath.charAt(i) == '/') {
                    uniqueFolders.add(fullPath.substring(0, i + 1));
                }
            }
        }
        return uniqueFolders;
    }


    //TODO кастомное исключение
    private StreamingResponseBody downloadFile(Long id, String path) {
        return outputStream -> {
            try (InputStream object = minioClientService.getObject(id, path)) {
                byte[] data = new byte[BUFFER_SIZE_1KB];
                int bytesRead;
                while ((bytesRead = object.read(data)) != END_OF_INPUT_STREAM) {
                    outputStream.write(data, START_OF_BUFFER, bytesRead);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error downloading file");
            }
        };
    }

    private StreamingResponseBody downloadFolder(Long id, String path) {
        return output -> {
            Iterable<Result<Item>> minioObjects = minioClientService.getListObjects(id, path, TraversalMode.RECURSIVE);
            List<Item> items = directoryService.extractAndFilterItemsFromMinio(minioObjects, id, path);
            buildZipFromItems(output, path, id, items);
        };
    }

    //TODO кастомные исключения!
    private void buildZipFromItems(OutputStream output, String path, Long id, List<Item> items) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(output)) {
            String parentPath = buildParentPath(path);
            Path parentDirectory = Paths.get(parentPath);

            for (Item item : items) {
                if (item.objectName().endsWith("/")) {
                    continue;
                }

                String pathWithoutRoot = deleteRootPath(item.objectName(), id);
                String relativePath = getRelativePath(pathWithoutRoot, parentDirectory);

                zip.putNextEntry(new ZipEntry(relativePath));

                try (InputStream object = minioClientService.getObject(id, pathWithoutRoot)) {
                    IOUtils.copy(object, zip);
                }
                zip.closeEntry();
            }
        }
    }

    private FileSystemItemResponseDto moveFile(String currentPath, String newPath, Long id) {
        String folderName = extractResourceName(newPath, false);
        long size = minioClientService.statObject(id, currentPath)
                .map(StatObjectResponse::size)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));

        minioClientService.copyObject(id, currentPath, newPath);
        delete(id, currentPath);

        return new FileSystemItemResponseDto(
                buildParentPath(newPath),
                folderName,
                size,
                ResourceType.FILE
        );
    }


    private FileSystemItemResponseDto moveFolder(String currentPath, String newPath, long id) {
        String folderName = extractResourceName(newPath, true);

        Iterable<Result<Item>> minioObjects = minioClientService.getListObjects(id, currentPath, TraversalMode.RECURSIVE);
        List<Item> items = directoryService.extractAndFilterItemsFromMinio(minioObjects, id, currentPath);

        if (items.isEmpty()) {
            minioClientService.copyObject(id, currentPath, newPath);
            minioClientService.putDirectory(id, newPath);
        }

        Set<String> uniqueFolders = getUniqueFolders(items, newPath);

        if (!uniqueFolders.isEmpty()) {
            for (String name : uniqueFolders) {
                minioClientService.putDirectory(id, name);
            }
        }

        for (Item item : items) {
            String relativeResourcePath = buildRelativeResourcePath(item, currentPath, id);
            String fullCurrentPath = currentPath + relativeResourcePath;
            String fullNewPath = newPath + relativeResourcePath;
            minioClientService.copyObject(id, fullCurrentPath, fullNewPath);
        }
        delete(id, currentPath);

        return new FileSystemItemResponseDto(
                buildParentPath(newPath),
                folderName,
                0L,
                ResourceType.DIRECTORY
        );

    }

    public boolean isResourceExists(Long id, String parentPath, String path) {
        List<FileSystemItemResponseDto> currentDirectory = directoryService.getDirectory(id, parentPath, TraversalMode.NON_RECURSIVE);
        boolean isTrailingSlash = checkTrailingSlash(parentPath, path);
        String resourceName = extractResourceName(path, isTrailingSlash);

        for (FileSystemItemResponseDto dto : currentDirectory) {
            if (dto.name().equals(resourceName)) {
                return true;
            }
        }
        return false;
    }

    //TODO подумай о дублировании названия метода (похож с прошлым)
    public boolean isResourceExists(Long id, String path, MultipartFile[] files) {
        if (files == null || files.length == 0 || files[0] == null) {
            return false;
        }

        MultipartFile file = files[0];
        String fileName = file.getOriginalFilename();
        String folderName = fileName.substring(0, fileName.indexOf("/") + 1);

        Iterable<Result<Item>> minioObjects = minioClientService.getListObjects(id, path, TraversalMode.NON_RECURSIVE);
        List<Item> items = directoryService.extractAndFilterItemsFromMinio(minioObjects, id, path);

        for (Item item : items) {
            String objectName = item.objectName();
            boolean isTrailingSlash = objectName.endsWith("/") || objectName.equals("");
            String resourceName = extractResourceName(objectName, isTrailingSlash);
            if (resourceName.equals(folderName)) {
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

    private List<FileSystemItemResponseDto> searchResources(List<Item> items, Long id, String query) {
        List<FileSystemItemResponseDto> queryResults = new ArrayList<>();

        for (Item item : items) {
            String relativePath = deleteRootPath(item.objectName(), id);
            boolean isTrailingSlash = relativePath.endsWith("/") || relativePath.equals("");
            String resourceName = extractResourceName(relativePath, isTrailingSlash);

            if (resourceName.toLowerCase().contains(query.toLowerCase())) {
                String parentPath = buildParentPath(relativePath);

                queryResults.add(new FileSystemItemResponseDto(
                        parentPath,
                        resourceName,
                        item.size(),
                        resourceName.endsWith("/") ? ResourceType.DIRECTORY : ResourceType.FILE
                ));
            }
        }
        return queryResults;
    }


}
