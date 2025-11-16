package org.example.cloudstorage;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.example.cloudstorage.dto.userDto.UserRegistrationRequestDto;
import org.example.cloudstorage.dto.userDto.UserResponseDto;
import org.example.cloudstorage.exception.UserExistsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceIT extends AbstractIntegrationTest {

    @Autowired
    private Validator validator;

    private final static int TOO_LONG_PASSWORD_SIZE = 21;

    @Test
    void shouldCreateUser() {
        UserRegistrationRequestDto user = new UserRegistrationRequestDto("Dimka", "password");
        Set<ConstraintViolation<UserRegistrationRequestDto>> violations = validator.validate(user);

        assertTrue(violations.isEmpty());
        UserResponseDto result = userService.create(user);
        assertNotNull(result);
        assertEquals("Dimka", result.username());
    }

    @Test
    void ShouldThrowUserExistsException() {
        UserRegistrationRequestDto firstUser = new UserRegistrationRequestDto("Dima", "pass");
        userService.create(firstUser);

        UserRegistrationRequestDto secondUser = new UserRegistrationRequestDto("Dima", "pass1");

        assertThrows(UserExistsException.class, () -> {
            userService.create(secondUser);
        });
    }

    @Test
    void shouldFailValidationWhenUsernameIsTooShort() {
        UserRegistrationRequestDto user = new UserRegistrationRequestDto("Dima", "password");

        Set<ConstraintViolation<UserRegistrationRequestDto>> violations = validator.validate(user);
        ConstraintViolation<UserRegistrationRequestDto> violation = violations.iterator().next();

        assertEquals("The username must be between 5 and 20 characters long", violation.getMessage());
    }

    @Test
    void shouldFailValidationWhenPasswordIsTooLong() {
        UserRegistrationRequestDto user = new UserRegistrationRequestDto(
                "Dimka",
                "A".repeat(TOO_LONG_PASSWORD_SIZE));
        Set<ConstraintViolation<UserRegistrationRequestDto>> violations = validator.validate(user);
        ConstraintViolation<UserRegistrationRequestDto> violation = violations.iterator().next();

        assertEquals("The password must be between 5 and 20 characters long", violation.getMessage());
    }

    @Test
    void shouldLoadUserByUsername() {
        UserRegistrationRequestDto user = new UserRegistrationRequestDto(
                "Dimka", "password");
        userService.create(user);

        UserDetails result = userService.loadUserByUsername(user.getUsername());

        assertNotNull(result);
        assertEquals("Dimka", result.getUsername());
    }

    @Test
    void shouldThrowBadCredentialsException() {
        UserRegistrationRequestDto user = new UserRegistrationRequestDto(
                "Dimka", "password");
        userService.create(user);

        assertThrows(BadCredentialsException.class, () -> {
            userService.loadUserByUsername("Pavel");
        });

    }

    @Test
    void passwordShouldBeEncoded() {
        UserRegistrationRequestDto user = new UserRegistrationRequestDto("Dimka", "password");
        userService.create(user);

        UserDetails result = userService.loadUserByUsername(user.getUsername());
        assertNotNull(result);
        assertNotEquals(user.getPassword(), result.getPassword());
    }

}
