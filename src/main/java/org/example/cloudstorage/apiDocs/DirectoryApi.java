package org.example.cloudstorage.apiDocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.cloudstorage.dto.ErrorResponseDto;
import org.example.cloudstorage.dto.fileSystemRequestDto.FileSystemPathRequestDto;
import org.example.cloudstorage.dto.resourceResponseDto.FolderResponseDto;
import org.example.cloudstorage.dto.resourceResponseDto.ResourceResponseDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@Tag(
        name = "Directory Management",
        description = "API for directory operations - creating folders and browsing directory contents"
)
public interface DirectoryApi {

    @Operation(
            summary = "The user can get info about directory",
            description = "Method takes path for directory and get information about all files."
    )

    @ApiResponses(value = {
            @ApiResponse(
                    description = "Successfully get info about folder!",
                    responseCode = "200",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResourceResponseDto.class, type = "array")
                    )
            ),
            @ApiResponse(
                    description = "Invalid or nonexistent path",
                    responseCode = "400",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    description = "The user is not authorized",
                    responseCode = "401",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    description = "Folder doesn't exists",
                    responseCode = "404",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
    }
    )

    ResponseEntity<List<ResourceResponseDto>> getDirectory(
            FileSystemPathRequestDto fileSystemDto,
            UserDetails userDetails);


    @Operation(
            summary = "Create new directory",
            description = "Create an empty directory at the specified path"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Directory successfully created",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FolderResponseDto.class)
                    )
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or missing path for the new directory",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authorized",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Parent directory does not exist",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Directory already exists",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),

    })
    ResponseEntity<FolderResponseDto> createDirectory(
            FileSystemPathRequestDto fileSystemDto,
            UserDetails userDetails);

}
