package org.example.cloudstorage;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.UserRegistrationRequestDto;
import org.example.cloudstorage.dto.UserResponseDto;
import org.example.cloudstorage.exception.UserExistsException;
import org.example.cloudstorage.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

//TODO навести порядок в этом классе. Конфиг вынести!!!
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
public class UserServiceIT {

    private final UserService userService;
    private final Validator validator;
    private final static int TOO_LONG_PASSWORD_SIZE = 21;
    private static final String MINIO_USERNAME = "minioadmin";
    private static final String MINIO_PASSWORD = "minioadmin";

    @Container
    private static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:latest");

    @Container
    private static final MinIOContainer minioContainer =
            new MinIOContainer("minio/minio:latest")
                    .withUserName(MINIO_USERNAME)
                    .withPassword(MINIO_PASSWORD);

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);

        registry.add("MINIO_URL", minioContainer::getS3URL);
        registry.add("MINIO_USER", minioContainer::getUserName);
        registry.add("MINIO_PASSWORD", minioContainer::getPassword);
        registry.add("MINIO_BUCKET_NAME", () -> "test-bucket");
    }


    @Test
    void shouldCreateUser(){
        UserRegistrationRequestDto user = new UserRegistrationRequestDto("Dimka", "password", "password");
        Set<ConstraintViolation<UserRegistrationRequestDto>> violations = validator.validate(user);

        assertTrue(violations.isEmpty());
        UserResponseDto result =  userService.create(user);
        assertNotNull(result);
        assertEquals("Dimka", result.username());
    }

    @Test
    void ShouldThrowUserExistsException(){
        UserRegistrationRequestDto firstUser = new UserRegistrationRequestDto("Dima", "pass", "pass");
        userService.create(firstUser);

        UserRegistrationRequestDto secondUser = new UserRegistrationRequestDto("Dima", "pass1", "pass1");

        assertThrows(UserExistsException.class, ()->{
            userService.create(secondUser);
        });
    }

    @Test
    void shouldFailValidationWhenUsernameIsTooShort(){
        UserRegistrationRequestDto user = new UserRegistrationRequestDto("Dima", "password", "password");

        Set<ConstraintViolation<UserRegistrationRequestDto>> violations = validator.validate(user);
        ConstraintViolation<UserRegistrationRequestDto> violation = violations.iterator().next();

        assertEquals("The username must be between 5 and 20 characters long", violation.getMessage());
    }

    @Test
    void shouldFailValidationWhenPasswordIsTooLong(){
        UserRegistrationRequestDto user = new UserRegistrationRequestDto(
                "Dimka",
                "A".repeat(TOO_LONG_PASSWORD_SIZE),
                "A".repeat(TOO_LONG_PASSWORD_SIZE));

        Set<ConstraintViolation<UserRegistrationRequestDto>> violations = validator.validate(user);
        ConstraintViolation<UserRegistrationRequestDto> violation = violations.iterator().next();

        assertEquals("The password must be between 5 and 20 characters long", violation.getMessage());
    }

    @Test
    void shouldFailValidationWhenPasswordDoesNotMatch(){
        UserRegistrationRequestDto user = new UserRegistrationRequestDto(
                "Dimka","password", "password2");

        Set<ConstraintViolation<UserRegistrationRequestDto>> violations = validator.validate(user);
        ConstraintViolation<UserRegistrationRequestDto> violation = violations.iterator().next();

        assertEquals("The passwords don't match", violation.getMessage());
    }

    @Test
    void shouldLoadUserByUsername(){
        UserRegistrationRequestDto user = new UserRegistrationRequestDto(
                "Dimka","password", "password");
        userService.create(user);

        UserDetails result = userService.loadUserByUsername(user.getUsername());

        assertNotNull(result);
        assertEquals("Dimka", result.getUsername());
    }

    @Test
    void shouldThrowBadCredentialsException(){
        UserRegistrationRequestDto user = new UserRegistrationRequestDto(
                "Dimka","password", "password");
        userService.create(user);

        assertThrows(BadCredentialsException.class, ()->{
            userService.loadUserByUsername("Pavel");
        });

    }

    @Test
    void passwordShouldBeEncoded(){
        UserRegistrationRequestDto user = new UserRegistrationRequestDto("Dimka", "password", "password");
        userService.create(user);

        UserDetails result = userService.loadUserByUsername(user.getUsername());
        assertNotNull(result);
        assertNotEquals(user.getPassword(), result.getPassword());
    }

}
