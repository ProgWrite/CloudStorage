package org.example.cloudstorage;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
@Transactional
public abstract class AbstractIntegrationTest {

    private static final String MINIO_USERNAME = "minioadmin";
    private static final String MINIO_PASSWORD = "minioadmin";
    private static final String MINIO_BUCKET_NAME = "test-bucket";

    @Container
    protected static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:latest");

    @Container
    protected static final MinIOContainer minioContainer =
            new MinIOContainer("minio/minio:latest")
                    .withUserName(MINIO_USERNAME)
                    .withPassword(MINIO_PASSWORD);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

        registry.add("MINIO_URL", minioContainer::getS3URL);
        registry.add("MINIO_USER", minioContainer::getUserName);
        registry.add("MINIO_PASSWORD", minioContainer::getPassword);
        registry.add("MINIO_BUCKET_NAME", () -> MINIO_BUCKET_NAME);

    }

}
