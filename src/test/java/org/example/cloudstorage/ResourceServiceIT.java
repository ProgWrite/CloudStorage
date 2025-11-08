package org.example.cloudstorage;


import org.example.cloudstorage.dto.FileSystemItemResponseDto;
import org.example.cloudstorage.dto.UserRegistrationRequestDto;
import org.example.cloudstorage.dto.UserResponseDto;
import org.example.cloudstorage.exception.InvalidPathException;
import org.example.cloudstorage.exception.ResourceNotFoundException;
import org.example.cloudstorage.service.DirectoryService;
import org.example.cloudstorage.service.ResourceService;
import org.example.cloudstorage.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import utils.TraversalMode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResourceServiceIT extends AbstractIntegrationTest {

    private static final int EXPECTED_FILES_BEFORE_DELETE = 5;
    private static final int EXPECTED_FILES_AFTER_DELETE = 2;


    @Autowired
    private UserService userService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private DirectoryService directoryService;


    private UserResponseDto testUser;
    private MultipartFile[] testFiles;
    private MultipartFile[] testFolder;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testFiles = createTestFile();
        testFolder = createTestFolder();
    }

    @Test
    void shouldUploadFileToMinioBucket() {
        String path = "";
        String expectedFileName = "test-file-1.txt";
        String resourcePath = path + expectedFileName;

        List<FileSystemItemResponseDto> uploadedResources = resourceService.upload(testUser.id(), path, testFiles);
        FileSystemItemResponseDto resourceInfo = resourceService.getResourceInfo(testUser.id(), resourcePath);

        assertNotNull(uploadedResources);
        assertEquals(expectedFileName, resourceInfo.name());
        assertEquals(path, resourceInfo.path());
    }

    @Test
    void shouldThrowResourceNotFoundException() {
        String path = "";
        String wrongFileName = "test-file-3.txt";
        String wrongResourcePath = path + wrongFileName;

        List<FileSystemItemResponseDto> uploadedResources = resourceService.upload(testUser.id(), path, testFiles);

        assertNotNull(uploadedResources);

        assertThrows(ResourceNotFoundException.class, () -> {
            resourceService.getResourceInfo(testUser.id(), wrongResourcePath);
        });

    }

    @Test
    void shouldThrowInvalidPathException() {
        String correctPath = "";
        String missingPath = "wrongPath/";
        String expectedFileName = "test-file-1.txt";
        String missingResourcePath = missingPath + expectedFileName;

        List<FileSystemItemResponseDto> uploadedResources = resourceService.upload(testUser.id(), correctPath, testFiles);

        assertNotNull(uploadedResources);

        assertThrows(InvalidPathException.class, () -> {
            resourceService.getResourceInfo(testUser.id(), missingResourcePath);
        });

    }

    @Test
    void shouldDeleteResource() {
        String path = "";
        String expectedFileName = "test-file-1.txt";
        String resourcePath = path + expectedFileName;

        resourceService.upload(testUser.id(), path, testFiles);

        FileSystemItemResponseDto resourceInfo = resourceService.getResourceInfo(testUser.id(), resourcePath);
        assertNotNull(resourceInfo);
        assertEquals(expectedFileName, resourceInfo.name());

        resourceService.delete(testUser.id(), resourcePath);

        assertThrows(InvalidPathException.class, () -> {
            resourceService.getResourceInfo(testUser.id(), resourcePath);
        });
    }

    @Test
    void shouldDeleteFolder() {
        String uploadedPath = "";
        String parentPath = "docs/";
        String pathForDelete = "docs/images/";

        resourceService.upload(testUser.id(), uploadedPath, testFolder);

        List<FileSystemItemResponseDto> files = directoryService.getDirectory(testUser.id(), parentPath, TraversalMode.RECURSIVE);
        assertEquals(EXPECTED_FILES_BEFORE_DELETE, files.size());

        resourceService.delete(testUser.id(), pathForDelete);

        List<FileSystemItemResponseDto> filesAfterDelete = directoryService.getDirectory(testUser.id(), parentPath, TraversalMode.RECURSIVE);
        assertEquals(EXPECTED_FILES_AFTER_DELETE, filesAfterDelete.size());

    }

    @Test
    void shouldThrowInvalidPathExceptionWhenDeleteResource() {
        String path = "";
        String invalidPath = "docs//";
        String expectedFileName = "test-file-1.txt";
        String resourcePath = path + expectedFileName;
        String wrongResourcePath = invalidPath + expectedFileName;

        resourceService.upload(testUser.id(), path, testFiles);

        FileSystemItemResponseDto resourceInfo = resourceService.getResourceInfo(testUser.id(), resourcePath);
        assertNotNull(resourceInfo);
        assertEquals(expectedFileName, resourceInfo.name());

        resourceService.delete(testUser.id(), resourcePath);

        assertThrows(InvalidPathException.class, () -> {
            resourceService.getResourceInfo(testUser.id(), wrongResourcePath);
        });
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenDeleteResource() {
        String path = "";
        String expectedFileName = "test-file-1.txt";
        String resourcePath = path + expectedFileName;
        String wrongResourceName = "test-file-3.txt";
        String wrongResourcePath = path + wrongResourceName;

        resourceService.upload(testUser.id(), path, testFiles);

        FileSystemItemResponseDto resourceInfo = resourceService.getResourceInfo(testUser.id(), resourcePath);
        assertNotNull(resourceInfo);
        assertEquals(expectedFileName, resourceInfo.name());

        assertThrows(ResourceNotFoundException.class, () -> {
            resourceService.delete(testUser.id(), wrongResourcePath);
        });
    }

    private UserResponseDto createTestUser() {
        UserRegistrationRequestDto user = new UserRegistrationRequestDto(
                "TestUser",
                "password",
                "password"
        );
        return userService.create(user);
    }

    private MultipartFile[] createTestFile() {
        return new MultipartFile[]{
                new MockMultipartFile(
                        "file1",
                        "test-file-1.txt",
                        "text/plain",
                        "Hello World 1".getBytes()
                )
        };
    }

    private MultipartFile[] createTestFolder() {
        return new MultipartFile[]{
                new MockMultipartFile(
                        "files", "docs/document1.txt", "text/plain", "Document 1 content".getBytes()
                ),
                new MockMultipartFile(
                        "files", "docs/document2.pdf", "application/pdf", "PDF content".getBytes()
                ),
                new MockMultipartFile(
                        "files", "docs/images/photo.jpg", "image/jpeg", "Fake image content".getBytes()
                ),
                new MockMultipartFile(
                        "files", "docs/images/vacation.jpg", "image/jpeg", "Fake image content".getBytes()
                )
        };
    }


}
