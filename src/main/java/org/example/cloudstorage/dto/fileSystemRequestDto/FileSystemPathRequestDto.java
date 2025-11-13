package org.example.cloudstorage.dto.fileSystemRequestDto;

import jakarta.validation.constraints.Pattern;

public record FileSystemPathRequestDto(

        @Pattern(regexp = "^[^\\\\:*?\"<>|]*$", message = "Path contains invalid characters")
        String path) {
}
