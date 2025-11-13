package org.example.cloudstorage.service;

import io.minio.Result;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.IOUtils;
import org.example.cloudstorage.dto.ResourceType;
import org.example.cloudstorage.dto.resourceResponseDto.FileResponseDto;
import org.example.cloudstorage.dto.resourceResponseDto.FolderResponseDto;
import org.example.cloudstorage.dto.resourceResponseDto.ResourceResponseDto;
import org.example.cloudstorage.exception.FileDownloadException;
import org.example.cloudstorage.exception.InvalidPathException;
import org.example.cloudstorage.exception.ResourceExistsException;
import org.example.cloudstorage.exception.ResourceNotFoundException;
import org.example.cloudstorage.mapper.FileSystemItemMapper;
import org.example.cloudstorage.validation.ValidationUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import utils.TraversalMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.example.cloudstorage.validation.ValidationUtils.validateResourceNameForUpload;
import static utils.PathUtils.*;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final MinioClientService minioClientService;
    private final DirectoryService directoryService;
    private final ValidationUtils validationUtils;
    private static final int BUFFER_SIZE_1KB = 1024;
    private static final int START_OF_BUFFER = 0;
    private static final int END_OF_INPUT_STREAM = -1;
    private static final String ROOT_PATH = "";

    public ResourceResponseDto getResourceInfo(Long id, String path) {
        String parentPath = buildParentPath(path);

        if (path.equals(ROOT_PATH) || path.contains("//")) {
            throw new InvalidPathException("Invalid path");
        }

        if (minioClientService.isPathExists(id, parentPath)) {
            return minioClientService.statObject(id, path)
                    .map(object -> FileSystemItemMapper.INSTANCE.statObjectToDto(object, id))
                    .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        }

        throw new InvalidPathException("path does not exist");
    }

    public List<ResourceResponseDto> upload(Long id, String path, MultipartFile[] files) {
        MultipartFile file = files[0];

        if (file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {
            throw new InvalidPathException("File list cannot be empty");
        }

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

    public StreamingResponseBody download(Long id, String path) {
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
    public ResourceResponseDto move(Long id, String currentPath, String newPath) {
        validationUtils.validateMoveOperation(id, currentPath, newPath);
        return newPath.endsWith("/") ?
                moveFolder(currentPath, newPath, id) :
                moveFile(currentPath, newPath, id);
    }

    public List<ResourceResponseDto> search(Long id, String query) {
        Iterable<Result<Item>> minioObjects = minioClientService.getListObjects(id, ROOT_PATH, TraversalMode.RECURSIVE);
        List<Item> items = directoryService.extractAndFilterItemsFromMinio(minioObjects, id, ROOT_PATH);

        return searchResources(items, id, query);
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

    private List<ResourceResponseDto> getUploadedFiles(MultipartFile[] files, Long id, String path) {
        List<ResourceResponseDto> uploadedFiles = new ArrayList<>();
        Set<String> uniqueFolders = getUniqueFolders(files, path, id);

        if (!uniqueFolders.isEmpty()) {
            for (String folderPath : uniqueFolders) {
                validateResourceNameForUpload(extractResourceName(folderPath, false));
                minioClientService.putDirectory(id, folderPath);
            }
        }

        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            validateResourceNameForUpload(fileName);
            minioClientService.putFile(id, path, file);
            uploadedFiles.add(new FileResponseDto(
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
        List<ResourceResponseDto> files = directoryService.getDirectory(id, path, TraversalMode.NON_RECURSIVE);
        for (ResourceResponseDto file : files) {
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

    private StreamingResponseBody downloadFile(Long id, String path) {
        return outputStream -> {
            try (InputStream object = minioClientService.getObject(id, path)) {
                byte[] data = new byte[BUFFER_SIZE_1KB];
                int bytesRead;
                while ((bytesRead = object.read(data)) != END_OF_INPUT_STREAM) {
                    outputStream.write(data, START_OF_BUFFER, bytesRead);
                }
            } catch (IOException e) {
                throw new FileDownloadException(
                        String.format("Failed to download file in path: '%s' for user %d", path, id)
                );
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

    private FileResponseDto moveFile(String currentPath, String newPath, Long id) {
        String folderName = extractResourceName(newPath, false);
        long size = minioClientService.statObject(id, currentPath)
                .map(StatObjectResponse::size)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));

        minioClientService.copyObject(id, currentPath, newPath);
        delete(id, currentPath);

        return new FileResponseDto(
                buildParentPath(newPath),
                folderName,
                size,
                ResourceType.FILE
        );
    }

    private FolderResponseDto moveFolder(String currentPath, String newPath, long id) {
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

        return new FolderResponseDto(
                buildParentPath(newPath),
                folderName,
                ResourceType.DIRECTORY
        );

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

    private List<ResourceResponseDto> searchResources(List<Item> items, Long id, String query) {
        List<ResourceResponseDto> queryResults = new ArrayList<>();

        for (Item item : items) {
            String relativePath = deleteRootPath(item.objectName(), id);
            boolean isTrailingSlash = relativePath.endsWith("/") || relativePath.equals("");
            String resourceName = extractResourceName(relativePath, isTrailingSlash);

            if (resourceName.toLowerCase().contains(query.toLowerCase())) {
                String parentPath = buildParentPath(relativePath);

                if (resourceName.endsWith("/")) {
                    queryResults.add(new FolderResponseDto(
                            parentPath,
                            resourceName,
                            ResourceType.DIRECTORY
                    ));
                } else {
                    queryResults.add(new FileResponseDto(
                            parentPath,
                            resourceName,
                            item.size(),
                            ResourceType.FILE
                    ));
                }

            }
        }
        return queryResults;
    }


}
