package org.example.cloudstorage;

import org.example.cloudstorage.dto.*;
import org.example.cloudstorage.dto.resourceResponseDto.FileResponseDto;
import org.example.cloudstorage.dto.resourceResponseDto.ResourceResponseDto;
import org.example.cloudstorage.exception.InvalidPathException;
import org.example.cloudstorage.exception.ResourceExistsException;
import org.example.cloudstorage.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import utils.TraversalMode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ResourceServiceIT extends AbstractIntegrationTest {

    private static final int EXPECTED_FILES_BEFORE_DELETE = 5;
    private static final int EXPECTED_FILES_AFTER_DELETE = 2;
    private static final int EXPECTED_FILES_BEFORE_MOVING = 2;
    private static final int EXPECTED_FILES_AFTER_MOVING = 3;
    private static final int EXPECTED_QUERY_RESULTS = 2;
    private static final int EXPECTED_QUERY_RESULTS_FOR_SECOND_USER = 3;

    @Nested
    class UploadTests {

        @Test
        void shouldUploadFileToMinioBucket() {
            String path = "";
            String expectedFileName = "test-file-1.txt";
            String resourcePath = path + expectedFileName;

            List<ResourceResponseDto> uploadedResources = resourceService.upload(testUser.id(), path, testFile);
            ResourceResponseDto resourceInfo = resourceService.getResourceInfo(testUser.id(), resourcePath);

            assertNotNull(uploadedResources);
            assertEquals(expectedFileName, resourceInfo.name());
            assertEquals(path, resourceInfo.path());
        }

        @Test
        void shouldThrowResourceNotFoundExceptionAfterUpload() {
            String path = "";
            String wrongFileName = "test-file-3.txt";
            String wrongResourcePath = path + wrongFileName;

            List<ResourceResponseDto> uploadedResources = resourceService.upload(testUser.id(), path, testFile);

            assertNotNull(uploadedResources);

            assertThrows(ResourceNotFoundException.class, () -> {
                resourceService.getResourceInfo(testUser.id(), wrongResourcePath);
            });

        }

        @Test
        void shouldThrowInvalidPathExceptionAfterUpload() {
            String correctPath = "";
            String missingPath = "wrongPath/";
            String expectedFileName = "test-file-1.txt";
            String missingResourcePath = missingPath + expectedFileName;

            List<ResourceResponseDto> uploadedResources = resourceService.upload(testUser.id(), correctPath, testFile);

            assertNotNull(uploadedResources);

            assertThrows(InvalidPathException.class, () -> {
                resourceService.getResourceInfo(testUser.id(), missingResourcePath);
            });

        }

        @Test
        void shouldThrowResourceExistsExceptionWhenUploadFile() {
            String uploadedPath = "";
            MultipartFile[] existedFile = new MultipartFile[]{
                    new MockMultipartFile(
                            "file1",
                            "test-file-1.txt",
                            "text/plain",
                            "Hello World 1".getBytes()
                    )
            };

            resourceService.upload(testUser.id(), uploadedPath, testFile);

            ResourceExistsException exception = assertThrows(ResourceExistsException.class, () -> {
                resourceService.upload(testUser.id(), uploadedPath, existedFile);
            });

            assertEquals("File with this name already exists", exception.getMessage());
        }

        @Test
        void shouldThrowResourceExistsExceptionWhenUploadFolder() {
            String uploadedPath = "";
            MultipartFile[] existedFolder = createExistedFolder();

            resourceService.upload(testUser.id(), uploadedPath, testFolder);

            ResourceExistsException exception = assertThrows(ResourceExistsException.class, () -> {
                resourceService.upload(testUser.id(), uploadedPath, existedFolder);
            });

            assertEquals("Resource with this name already exists in this directory", exception.getMessage());

        }

        @Test
        void shouldCreateNewFoldedAfterUpload() {
            String uploadedPath = "";
            MultipartFile[] newFolder = createNewTestFolder();
            String expectedPathForFolder = "memories/";
            String expectedPathForFile = "memories/best.txt";

            resourceService.upload(testUser.id(), uploadedPath, testFolder);
            resourceService.upload(testUser.id(), uploadedPath, newFolder);

            ResourceResponseDto uploadedFile = resourceService.getResourceInfo(testUser.id(), expectedPathForFile);
            assertNotNull(uploadedFile);

            List<ResourceResponseDto> uploadedDirectory = directoryService.getDirectory(testUser.id(), expectedPathForFolder, TraversalMode.NON_RECURSIVE);
            assertNotNull(uploadedDirectory);

        }

        @Test
        void shouldPreventUserFromReadingOtherUsersResources() {
            String uploadedPath = "";
            String expectedPathForFile = "docs/document1.txt";
            UserResponseDto secondUser = createSecondTestUser();

            resourceService.upload(testUser.id(), uploadedPath, testFolder);

            ResourceResponseDto uploadedFile = resourceService.getResourceInfo(testUser.id(), expectedPathForFile);
            assertNotNull(uploadedFile);

            InvalidPathException exception = assertThrows(InvalidPathException.class, () -> {
                resourceService.getResourceInfo(secondUser.id(), expectedPathForFile);
            });

            assertEquals("path does not exist", exception.getMessage());
        }

    }

    @Nested
    class DeleteTests {

        @Test
        void shouldDeleteResource() {
            String path = "";
            String expectedFileName = "test-file-1.txt";
            String resourcePath = path + expectedFileName;

            resourceService.upload(testUser.id(), path, testFile);

            ResourceResponseDto resourceInfo = resourceService.getResourceInfo(testUser.id(), resourcePath);
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

            List<ResourceResponseDto> files = directoryService.getDirectory(testUser.id(), parentPath, TraversalMode.RECURSIVE);
            assertEquals(EXPECTED_FILES_BEFORE_DELETE, files.size());

            resourceService.delete(testUser.id(), pathForDelete);

            List<ResourceResponseDto> filesAfterDelete = directoryService.getDirectory(testUser.id(), parentPath, TraversalMode.RECURSIVE);
            assertEquals(EXPECTED_FILES_AFTER_DELETE, filesAfterDelete.size());

        }

        @Test
        void shouldThrowInvalidPathExceptionWhenDeleteResource() {
            String path = "";
            String invalidPath = "docs//";
            String expectedFileName = "test-file-1.txt";
            String resourcePath = path + expectedFileName;
            String wrongResourcePath = invalidPath + expectedFileName;

            resourceService.upload(testUser.id(), path, testFile);

            ResourceResponseDto resourceInfo = resourceService.getResourceInfo(testUser.id(), resourcePath);
            assertNotNull(resourceInfo);
            assertEquals(expectedFileName, resourceInfo.name());

            resourceService.delete(testUser.id(), resourcePath);

            assertThrows(InvalidPathException.class, () -> {
                resourceService.getResourceInfo(testUser.id(), wrongResourcePath);
            });
        }

        @Test
        void shouldThrowResourceNotFoundExceptionWhenDeleteFile() {
            String path = "";
            String expectedFileName = "test-file-1.txt";
            String resourcePath = path + expectedFileName;
            String wrongResourceName = "test-file-3.txt";
            String wrongResourcePath = path + wrongResourceName;

            resourceService.upload(testUser.id(), path, testFile);

            ResourceResponseDto resourceInfo = resourceService.getResourceInfo(testUser.id(), resourcePath);
            assertNotNull(resourceInfo);
            assertEquals(expectedFileName, resourceInfo.name());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> resourceService.delete(testUser.id(), wrongResourcePath)
            );

            assertEquals("File with this name not found", exception.getMessage());
        }

        @Test
        void shouldThrowResourceNotFoundExceptionWhenDeleteFolder() {
            String uploadedPath = "";
            String nonExistentPath = "works/";

            resourceService.upload(testUser.id(), uploadedPath, testFolder);

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> resourceService.delete(testUser.id(), nonExistentPath)
            );

            assertEquals("Folder with this name not found", exception.getMessage());
        }

    }

    @Nested
    class DownloadTests {

        @Test
        void shouldDownloadFileWithCorrectContent() throws Exception {
            String path = "";
            String fileName = "test-file-1.txt";
            String filePath = path + fileName;
            String expectedContent = "Hello World 1";

            resourceService.upload(testUser.id(), path, testFile);
            StreamingResponseBody responseBody = resourceService.download(testUser.id(), filePath);

            assertNotNull(responseBody);

            MockHttpServletResponse response = new MockHttpServletResponse();
            responseBody.writeTo(response.getOutputStream());

            assertEquals(expectedContent, response.getContentAsString());
        }

        @Test
        void shouldDownloadFolderAsZipArchive() throws Exception {
            String uploadedPath = "";
            String folderPath = "docs/";
            resourceService.upload(testUser.id(), uploadedPath, testFolder);

            StreamingResponseBody responseBody = resourceService.download(testUser.id(), folderPath);

            MockHttpServletResponse response = new MockHttpServletResponse();
            responseBody.writeTo(response.getOutputStream());

            assertTrue(response.getContentAsByteArray().length > 0);
        }

        @Test
        void shouldThrowResourceNotFoundExceptionWhenDownloadFolder() {
            String uploadedPath = "";
            String nonExistentPath = "works/";

            resourceService.upload(testUser.id(), uploadedPath, testFolder);

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> resourceService.download(testUser.id(), nonExistentPath)
            );

            assertEquals("Folder with this name not found", exception.getMessage());
        }

        @Test
        void shouldThrowResourceNotFoundExceptionWhenDownloadFile() {
            String uploadedPath = "";
            String nonExistentFileName = "fakeFile.txt";
            String nonExistentPath = uploadedPath + nonExistentFileName;

            resourceService.upload(testUser.id(), uploadedPath, testFile);

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> resourceService.download(testUser.id(), nonExistentPath)
            );

            assertEquals("File with this name not found", exception.getMessage());
        }
    }

    @Nested
    class MoveAndRenameTests {

        @Test
        void shouldRenameFile() {
            String uploadedPath = "";
            String currentFileName = "test-file-1.txt";
            String newFileName = "books.txt";
            String currentPath = uploadedPath + currentFileName;
            String newPath = uploadedPath + newFileName;

            resourceService.upload(testUser.id(), uploadedPath, testFile);
            resourceService.move(testUser.id(), currentPath, newPath);
            ResourceResponseDto resourceInfo = resourceService.getResourceInfo(testUser.id(), newPath);


            assertEquals(newFileName, resourceInfo.name());

            assertThrows(ResourceNotFoundException.class, () -> {
                resourceService.getResourceInfo(testUser.id(), currentPath);
            });

        }

        @Test
        void shouldMoveFileToAnotherFolder() {
            String uploadedPath = "";
            String movingFolder = "docs/images/";
            String movingPath = uploadedPath + movingFolder;
            String currentPath = uploadedPath + "docs/document1.txt";
            String newPath = uploadedPath + "docs/images/document1.txt";

            resourceService.upload(testUser.id(), uploadedPath, testFolder);


            List<ResourceResponseDto> filesBeforeMoving = directoryService.getDirectory(testUser.id(), movingPath, TraversalMode.NON_RECURSIVE);
            assertEquals(EXPECTED_FILES_BEFORE_MOVING, filesBeforeMoving.size());

            resourceService.move(testUser.id(), currentPath, newPath);

            List<ResourceResponseDto> filesAfterMoving = directoryService.getDirectory(testUser.id(), movingPath, TraversalMode.NON_RECURSIVE);
            assertEquals(EXPECTED_FILES_AFTER_MOVING, filesAfterMoving.size());


            assertThrows(ResourceNotFoundException.class, () -> {
                resourceService.getResourceInfo(testUser.id(), currentPath);
            });

        }

        @Test
        void shouldRenameFolder() {
            String uploadedPath = "";
            String currentFolderName = "docs/images/";
            String newFolderName = "docs/reports/";
            String expectedFileNameAfterRename = "docs/reports/photo.jpg";
            String currentPath = uploadedPath + currentFolderName;
            String newPath = uploadedPath + newFolderName;

            resourceService.upload(testUser.id(), uploadedPath, testFolder);
            resourceService.move(testUser.id(), currentPath, newPath);

            List<ResourceResponseDto> resources = directoryService.getDirectory(testUser.id(), newPath, TraversalMode.NON_RECURSIVE);
            assertNotNull(resources);

            ResourceResponseDto renamedFileInFolder = resourceService.getResourceInfo(testUser.id(), uploadedPath + expectedFileNameAfterRename);
            assertNotNull(renamedFileInFolder);
        }

        @Test
        void shouldMoveFolderToAnotherFolder() {
            String uploadedPath = "";
            String movingFolder = "images/";
            String movingPath = "docs/images/";
            String currentPath = uploadedPath + movingPath;
            String newPath = uploadedPath + movingFolder;
            String expectedFileNameAfterRename = "images/photo.jpg";

            resourceService.upload(testUser.id(), uploadedPath, testFolder);
            resourceService.move(testUser.id(), currentPath, newPath);

            List<ResourceResponseDto> resources = directoryService.getDirectory(testUser.id(), newPath, TraversalMode.NON_RECURSIVE);
            assertNotNull(resources);

            ResourceResponseDto renamedFileInFolder = resourceService.getResourceInfo(testUser.id(), uploadedPath + expectedFileNameAfterRename);
            assertNotNull(renamedFileInFolder);

            assertThrows(ResourceNotFoundException.class, () -> {
                resourceService.getResourceInfo(testUser.id(), currentPath);
            });

        }

        @Test
        void shouldThrowExceptionForNonExistentCurrentPath() {
            String uploadedPath = "";
            String nonExistentCurrentPath = "secrets/salaries/";
            String movingFolder = "salaries/";
            String newPath = uploadedPath + movingFolder;

            resourceService.upload(testUser.id(), uploadedPath, testFolder);

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> resourceService.move(testUser.id(), nonExistentCurrentPath, newPath));

            assertEquals("Current path with this name not found", exception.getMessage());

        }

        @Test
        void shouldThrowExceptionForNonExistentNewPath() {
            String uploadedPath = "";
            String movingPath = "docs/images/";
            String currentPath = uploadedPath + movingPath;
            String nonExistentNewPath = "secrets/salaries/";

            resourceService.upload(testUser.id(), uploadedPath, testFolder);

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> resourceService.move(testUser.id(), currentPath, nonExistentNewPath));

            assertEquals("New path with this name not found", exception.getMessage());

        }

        @Test
        void shouldThrowExceptionForNonExistentResourceName() {
            String uploadedPath = "";
            String nonExistentResourceName = "salaries.txt";
            String newFileName = "books.txt";
            String currentPath = uploadedPath + nonExistentResourceName;
            String newPath = uploadedPath + newFileName;

            resourceService.upload(testUser.id(), uploadedPath, testFolder);

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> resourceService.move(testUser.id(), currentPath, newPath));

            assertEquals("Resource with this name not found", exception.getMessage());

        }

        @Test
        void shouldThrowExceptionWhenMovingFileToFolderWithoutFileName() {
            String uploadedPath = "";

            String currentPath = uploadedPath + "docs/document1.txt";
            String newPath = uploadedPath + "docs/images/";

            resourceService.upload(testUser.id(), uploadedPath, testFolder);

            InvalidPathException exception = assertThrows(
                    InvalidPathException.class,
                    () -> resourceService.move(testUser.id(), currentPath, newPath));

            assertEquals("New path should end with resource name", exception.getMessage());

        }

        @Test
        void shouldThrowExceptionWhenMovingFolderToFolderWithoutTrailingSlash() {
            String uploadedPath = "";
            String wrongMovingFolderName = "images";
            String movingPath = "docs/images/";
            String currentPath = uploadedPath + movingPath;
            String newPath = uploadedPath + wrongMovingFolderName;

            resourceService.upload(testUser.id(), uploadedPath, testFolder);

            InvalidPathException exception = assertThrows(
                    InvalidPathException.class,
                    () -> resourceService.move(testUser.id(), currentPath, newPath));

            assertEquals("New path for folders should end with /", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenMovingFileToFolderAndChangeFileName() {
            String uploadedPath = "";
            String currentPath = uploadedPath + "docs/document1.txt";
            String newPath = uploadedPath + "docs/images/renamedFile.txt";

            resourceService.upload(testUser.id(), uploadedPath, testFolder);

            InvalidPathException exception = assertThrows(
                    InvalidPathException.class,
                    () -> resourceService.move(testUser.id(), currentPath, newPath));

            assertEquals("Cannot change resource name during move operation", exception.getMessage());

        }

        @Test
        void shouldThrowExceptionWhenMovingFolderToOwnSubfolder() {
            String uploadedPath = "";
            String movingFolder = "docs/";
            String movingPath = "docs/images/docs/";
            String currentPath = uploadedPath + movingFolder;
            String newPath = uploadedPath + movingPath;

            resourceService.upload(testUser.id(), uploadedPath, testFolder);

            InvalidPathException exception = assertThrows(
                    InvalidPathException.class,
                    () -> resourceService.move(testUser.id(), currentPath, newPath));

            assertEquals("Cannot move folder into its own subfolder", exception.getMessage());
        }

        @Test
        void shouldThrowResourceExistsExceptionWhenRenameFile() {
            String uploadedPath = "";
            String movingFilePath = "docs/images/photo.jpg";
            String existingFilePath = "docs/images/vacation.jpg";
            String currentPath = uploadedPath + movingFilePath;
            String newPath = uploadedPath + existingFilePath;

            resourceService.upload(testUser.id(), uploadedPath, testFolder);

            ResourceExistsException exception = assertThrows(
                    ResourceExistsException.class,
                    () -> resourceService.move(testUser.id(), currentPath, newPath));

            assertEquals("Resource with this name already exists", exception.getMessage());
        }

    }

    @Nested
    class SearchTests {

        @Test
        void shouldSearchResources() {
            String uploadedPath = "";
            String query = "document";

            resourceService.upload(testUser.id(), uploadedPath, testFolder);

            List<ResourceResponseDto> queryResults = resourceService.search(testUser.id(), query);
            assertEquals(EXPECTED_QUERY_RESULTS, queryResults.size());

        }

        @Test
        void shouldSearchOnlyOwnResources() {
            String uploadedPath = "";
            String query = "document";
            UserResponseDto secondUser = createSecondTestUser();
            MultipartFile[] folderForSecondUser = createTestFolderForSecondUser();

            resourceService.upload(testUser.id(), uploadedPath, testFolder);
            resourceService.upload(secondUser.id(), uploadedPath, folderForSecondUser);

            List<ResourceResponseDto> queryResultsFirstUser = resourceService.search(testUser.id(), query);
            List<ResourceResponseDto> queryResultsSecondUser = resourceService.search(secondUser.id(), query);
            assertEquals(EXPECTED_QUERY_RESULTS, queryResultsFirstUser.size());
            assertEquals(EXPECTED_QUERY_RESULTS_FOR_SECOND_USER, queryResultsSecondUser.size());
        }

    }

    private UserResponseDto createSecondTestUser() {
        UserRegistrationRequestDto user = new UserRegistrationRequestDto(
                "TestUser2",
                "password",
                "password"
        );
        return userService.create(user);
    }

    private MultipartFile[] createExistedFolder() {
        return new MultipartFile[]{
                new MockMultipartFile(
                        "files", "docs/document15.txt", "text/plain", "Document 1 content".getBytes()
                )
        };
    }

    private MultipartFile[] createNewTestFolder() {
        return new MultipartFile[]{
                new MockMultipartFile(
                        "files", "memories/best.txt", "text/plain", "memories content".getBytes()
                )
        };
    }

    private MultipartFile[] createTestFolderForSecondUser() {
        return new MultipartFile[]{
                new MockMultipartFile(
                        "files", "docs/document1.txt", "text/plain", "Document 1 content".getBytes()
                ),
                new MockMultipartFile(
                        "files", "docs/document2.pdf", "application/pdf", "PDF content".getBytes()
                ),
                new MockMultipartFile(
                        "files", "docs/images/document3.pdf", "application/pdf", "Fake image content".getBytes()
                ),
                new MockMultipartFile(
                        "files", "docs/images/vacation.jpg", "image/jpeg", "Fake image content".getBytes()
                )
        };
    }

}
