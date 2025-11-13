package org.example.cloudstorage.dto.fileSystemRequestDto;


import jakarta.validation.constraints.Pattern;

public record FileSystemMoveRequestDto(
        @Pattern(regexp = "^[^\\\\:*?\"<>|]*$", message = "Path contains invalid characters")
        String from,
        @Pattern(regexp = "^[^\\\\:*?\"<>|]*$", message = "Path contains invalid characters")
        String to) {
}
