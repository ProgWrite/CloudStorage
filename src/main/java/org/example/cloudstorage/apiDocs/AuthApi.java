package org.example.cloudstorage.apiDocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.cloudstorage.dto.ErrorResponseDto;
import org.example.cloudstorage.dto.userDto.UserAuthorizationRequestDto;
import org.example.cloudstorage.dto.userDto.UserRegistrationRequestDto;
import org.example.cloudstorage.dto.userDto.UserResponseDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(
        name = "Auth management",
        description = "API for authentication, registration and authorization."
)
public interface AuthApi {

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
                    description = "Bad Request",
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
    ResponseEntity<UserResponseDto> signIn(
            @RequestBody UserAuthorizationRequestDto user,
            HttpServletRequest request,
            HttpServletResponse response
    );


    @Operation(
            summary = "The user can create an account",
            description = "Registration method takes JSON with credentials, register new user in system and creates session for user.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User credentials",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserRegistrationRequestDto.class),
                            examples = @ExampleObject(value = "{ \"username\": \"Dmitry\", \"password\": \"superPass\"}"
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
                    description = "Bad Request",
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
    ResponseEntity<UserResponseDto> signUp(
            UserRegistrationRequestDto user,
            HttpServletRequest request,
            HttpServletResponse response);


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
    ResponseEntity<Void> signOut(HttpServletRequest request);

}

