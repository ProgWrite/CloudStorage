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

    //TODO заглушка для Size и для Path и для ResourceType.FILE (просто тест)
    public FileSystemItemResponseDto move(Long id, String currentPath, String newPath) {
        String folderName = extractResourceName(newPath, false);
        minioClientService.copyObject(id, currentPath, newPath);
        delete(id, currentPath);

        return new FileSystemItemResponseDto(
                newPath,
                folderName,
                0L,
                ResourceType.FILE
        );
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
                String resourcePath = folderName;
                minioClientService.putDirectory(id, resourcePath);
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


}
