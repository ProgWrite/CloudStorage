package org.example.cloudstorage;


import org.example.cloudstorage.dto.resourceResponseDto.ResourceResponseDto;
import org.example.cloudstorage.dto.resourceResponseDto.FolderResponseDto;
import org.example.cloudstorage.exception.InvalidPathException;
import org.example.cloudstorage.exception.ResourceExistsException;
import org.example.cloudstorage.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.example.cloudstorage.model.TraversalMode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DirectoryServiceIT extends AbstractIntegrationTest {
    private static final int EXPECTED_ROOT_DIRECTORIES_NON_RECURSIVE = 1;
    private static final int EXPECTED_ROOT_DIRECTORIES_RECURSIVE = 6;

    @Nested
    class InformationAboutDirectoryTests {

        @Test
        void shouldCreateDirectory() {
            String uploadedPath = "";

            resourceService.upload(userId, uploadedPath, testFolder);

            List<ResourceResponseDto> rootDirectoryNonRecursive =
                    directoryService.getDirectory(userId, uploadedPath, TraversalMode.NON_RECURSIVE);

            List<ResourceResponseDto> rootDirectoryRecursive =
                    directoryService.getDirectory(userId, uploadedPath, TraversalMode.RECURSIVE);


            assertNotNull(rootDirectoryNonRecursive);
            assertNotNull(rootDirectoryRecursive);
            assertEquals(EXPECTED_ROOT_DIRECTORIES_NON_RECURSIVE, rootDirectoryNonRecursive.size());
            assertEquals(EXPECTED_ROOT_DIRECTORIES_RECURSIVE, rootDirectoryRecursive.size());
        }

        @Test
        void shouldThrowResourceNotFoundException() {
            String uploadedPath = "";
            String nonExistentPath = "salaries/";

            resourceService.upload(userId, uploadedPath, testFolder);

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                directoryService.getDirectory(userId, nonExistentPath, TraversalMode.RECURSIVE);
            });

            assertEquals("Folder with this name not found", exception.getMessage());

        }

        @Test
        void shouldThrowInvalidPathException() {
            String uploadedPath = "";
            String invalidPath = "docs//";

            resourceService.upload(userId, uploadedPath, testFolder);

            InvalidPathException exception = assertThrows(InvalidPathException.class, () -> {
                directoryService.getDirectory(userId, invalidPath, TraversalMode.RECURSIVE);
            });

            assertEquals("Invalid path", exception.getMessage());

        }
    }

    @Nested
    class createDirectoryTests {

        @Test
        void shouldCreateDirectory() {
            String uploadedPath = "";
            String folderName = "pictures";
            String fullPath = uploadedPath + folderName + "/";

            resourceService.upload(userId, uploadedPath, testFolder);
            FolderResponseDto folder = directoryService.createDirectory(userId, fullPath);

            assertNotNull(folder);
            assertEquals(folderName, folder.name());
        }

        @Test
        void shouldThrowExceptionIfParentPathNotFound() {
            String uploadedPath = "";
            String nonExistentPath = "dreams/salaries/";

            resourceService.upload(userId, uploadedPath, testFolder);

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                directoryService.createDirectory(userId, nonExistentPath);
            });

            assertEquals("Parent path not found.", exception.getMessage());

        }

        @Test
        void shouldThrowResourceExistsException() {
            String uploadedPath = "";
            String existentPath = "docs/images/";

            resourceService.upload(userId, uploadedPath, testFolder);

            ResourceExistsException exception = assertThrows(ResourceExistsException.class, () -> {
                directoryService.createDirectory(userId, existentPath);
            });

            assertEquals("Folder with this name already exists.", exception.getMessage());

        }

    }

}
