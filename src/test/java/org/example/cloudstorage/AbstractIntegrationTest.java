package org.example.cloudstorage;


import org.example.cloudstorage.dto.userDto.UserRegistrationRequestDto;
import org.example.cloudstorage.repository.UserRepository;
import org.example.cloudstorage.service.DirectoryService;
import org.example.cloudstorage.service.ResourceService;
import org.example.cloudstorage.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
@Transactional
public abstract class AbstractIntegrationTest {


    private static final String MINIO_USERNAME = "minioadmin";
    private static final String MINIO_PASSWORD = "minioadmin";
    private static final String MINIO_BUCKET_NAME = "test-bucket";

    @Container
    protected static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:latest")
                    .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 2))
                    .withStartupTimeout(Duration.ofMinutes(2));

    @Container
    protected static final GenericContainer<?> MINIO_CONTAINER =
            new GenericContainer<>("minio/minio:latest")
                    .withExposedPorts(9000)
                    .withEnv("MINIO_ROOT_USER", MINIO_USERNAME)
                    .withEnv("MINIO_ROOT_PASSWORD", MINIO_PASSWORD)
                    .withCommand("server /data")
                    .withAccessToHost(true)
                    .waitingFor(Wait.forHttp("/minio/health/ready").forPort(9000));

    @Autowired
    protected UserService userService;

    @Autowired
    protected ResourceService resourceService;

    @Autowired
    protected DirectoryService directoryService;

    @Autowired
    protected UserRepository userRepository;

    protected Long userId;
    protected MultipartFile[] testFile;
    protected MultipartFile[] testFolder;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);

        registry.add("MINIO_URL", () ->
                "http://" + MINIO_CONTAINER.getHost() + ":" + MINIO_CONTAINER.getMappedPort(9000));
        registry.add("MINIO_USER", () -> MINIO_USERNAME);
        registry.add("MINIO_PASSWORD", () -> MINIO_PASSWORD);
        registry.add("MINIO_BUCKET_NAME", () -> MINIO_BUCKET_NAME);

    }

    @BeforeEach
    void setUp() {
        userId = createUserAndGetId();
        testFile = createTestFile();
        testFolder = createTestFolder();
    }

    protected Long createUserAndGetId() {
        UserRegistrationRequestDto user = new UserRegistrationRequestDto(
                "TestUser",
                "password"
        );

        userService.create(user);
        return userRepository.findIdByUsername(user.getUsername());
    }



    protected MultipartFile[] createTestFile() {
        return new MultipartFile[]{
                new MockMultipartFile(
                        "file1",
                        "test-file-1.txt",
                        "text/plain",
                        "Hello World 1".getBytes()
                )
        };
    }

    protected MultipartFile[] createTestFolder() {
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
