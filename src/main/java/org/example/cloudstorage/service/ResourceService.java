package org.example.cloudstorage.service;

import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.FileSystemItemResponseDto;
import org.example.cloudstorage.dto.ResourceType;
import org.example.cloudstorage.exception.InvalidPathException;
import org.example.cloudstorage.exception.ResourceExistsException;
import org.example.cloudstorage.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import utils.PathUtils;

import java.util.*;

import static utils.PathUtils.*;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final MinioClientService minioClientService;
    private final DirectoryService directoryService;

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

        String folderName = extractFolderName(path, false);

        if (directoryService.isFolderExists(id, folderName, path)) {
            throw new ResourceExistsException("Folder with this name already exists.");
        }

        return getUploadedFiles(files, id, path);
    }

    public void delete(Long id, String path) {
        if(!isPathValidToDelete(path)){
            throw new InvalidPathException("Invalid path");
        }

        if(path.endsWith("/") || path.equals("")){
            directoryService.getDirectory(id,path);
            deleteFolder(id, path);
        }else{
            getResourceInfo(id, path);
            minioClientService.removeObject(id, path);
        }

    }


    private FileSystemItemResponseDto buildDto(StatObjectResponse object, Long id) {

        String fullName = object.object();
        String relativePath = deleteRootPath(fullName, id);
        String truePath = PathUtils.buildParentPath(relativePath);

        String folderName = extractFolderName(fullName, false);

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

    private List<FileSystemItemResponseDto> getUploadedFiles(MultipartFile[] files, Long id, String path) {

        List<FileSystemItemResponseDto> uploadedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();

          if(buildParentPath(fileName).endsWith("/")){
              Set<String> uniqueFolders = getUniqueFolders(files);
              for (String folderName : uniqueFolders) {
                  minioClientService.putDirectory(id, folderName);
              }
            }

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

    private void deleteFolder(Long id, String path){
        List<FileSystemItemResponseDto> files  = directoryService.getDirectory(id, path);
        for (FileSystemItemResponseDto file : files) {
            String pathForDelete = path + file.name();
            if(pathForDelete.endsWith("/")) {
                deleteFolder(id, pathForDelete);
            }else{
                minioClientService.removeObject(id, pathForDelete);
            }
        }
        minioClientService.removeObject(id, path);
    }

    private Set<String> getUniqueFolders(MultipartFile[] files){
        Set<String> uniqueFolders = new HashSet<>();

        for (MultipartFile file : files) {
            String folderName = file.getOriginalFilename();
            String parentPath =  buildParentPath(folderName);
            uniqueFolders.add(parentPath);
        }
        return uniqueFolders;
    }
}
