package org.example.cloudstorage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.dto.ErrorResponseDto;
import org.example.cloudstorage.dto.userDto.UserAuthorizationRequestDto;
import org.example.cloudstorage.dto.userDto.UserRegistrationRequestDto;
import org.example.cloudstorage.dto.userDto.UserResponseDto;
import org.example.cloudstorage.repository.UserRepository;
import org.example.cloudstorage.service.DirectoryService;
import org.example.cloudstorage.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor

@Tag(
        name = "Auth management",
        description = "Authentication, registration and authorization class."
)
public class AuthController {

    private final SecurityContextRepository securityContextRepository;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final DirectoryService directoryService;
    private final UserRepository userRepository;


    @Operation(
            summary = "The user can log into the account",
            description = "Login method takes JSON with credentials and creates session for user.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User credentials",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserAuthorizationRequestDto.class),
                            examples = @ExampleObject(value = "{ \"username\": \"Dmitry\", \"password\": \"superPass\" }"
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    description = "Successfully logged in",
                    responseCode = "200",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponseDto.class)
                    )
            ),
            @ApiResponse(
                    description = "MoveOperationValidator error",
                    responseCode = "400",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    description = "Wrong credentials",
                    responseCode = "401",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
    }
    )
    @PostMapping("/sign-in")
    public ResponseEntity<UserResponseDto> signIn(@RequestBody UserAuthorizationRequestDto user, HttpServletRequest request,
                                                  HttpServletResponse response) {
        log.info("Attempting authentication for user: {}", user.getUsername());
        authenticateUser(user.getUsername(), user.getPassword(), request, response);
        UserResponseDto responseDto = new UserResponseDto(user.getUsername());
        return ResponseEntity.ok(responseDto);
    }


    @Operation(
            summary = "The user can create an account",
            description = "Registration method takes JSON with credentials, register new user in system and creates session for user.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User credentials",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserRegistrationRequestDto.class),
                            examples = @ExampleObject(value = "{ \"username\": \"Dmitry\", \"password\": \"superPass\", \"confirmPassword\": \"superPass\"}"
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    description = "Registration success!",
                    responseCode = "201",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponseDto.class)
                    )
            ),
            @ApiResponse(
                    description = "MoveOperationValidator error",
                    responseCode = "400",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    description = "Username already exists",
                    responseCode = "409",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
    }
    )

    @PostMapping("/sign-up")
    public ResponseEntity<UserResponseDto> signUp(@Valid @RequestBody UserRegistrationRequestDto user, HttpServletRequest request,
                                                  HttpServletResponse response) {
        log.info("Attempting registration for user: {}", user.getUsername());
        UserResponseDto userDto = userService.create(user);
        authenticateUser(user.getUsername(), user.getPassword(), request, response);
        Long id = userRepository.findIdByUsername(user.getUsername());
        directoryService.createRootDirectory(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }


    @Operation(
            summary = "Logout method",
            description = "The user logs out of the account, the session is deleted"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    description = "Logout success!",
                    responseCode = "204"
            ),
            @ApiResponse(
                    description = "The user is not authorized",
                    responseCode = "401",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    }
    )

    @PostMapping("/sign-out")
    public ResponseEntity<Void> signOut(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        log.info("User logged out. Session invalidated.");
        return ResponseEntity.noContent().build();
    }

    private void authenticateUser(String username, String password, HttpServletRequest request,
                                  HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }

}
