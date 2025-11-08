package org.example.cloudstorage;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
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
    protected static final GenericContainer<?> minioContainer =
            new GenericContainer<>("minio/minio:latest")
                    .withExposedPorts(9000)
                    .withEnv("MINIO_ROOT_USER", MINIO_USERNAME)
                    .withEnv("MINIO_ROOT_PASSWORD", MINIO_PASSWORD)
                    .withCommand("server /data")
                    .withAccessToHost(true)
                    .waitingFor(Wait.forHttp("/minio/health/ready").forPort(9000));


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

        registry.add("MINIO_URL", () ->
                "http://" + minioContainer.getHost() + ":" + minioContainer.getMappedPort(9000));
        registry.add("MINIO_USER", () -> MINIO_USERNAME);
        registry.add("MINIO_PASSWORD", () -> MINIO_PASSWORD);
        registry.add("MINIO_BUCKET_NAME", () -> MINIO_BUCKET_NAME);

    }

}
