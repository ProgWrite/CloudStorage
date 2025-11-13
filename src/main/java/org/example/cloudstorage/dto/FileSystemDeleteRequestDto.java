package org.example.cloudstorage.dto;

import jakarta.validation.constraints.Pattern;


public record FileSystemDeleteRequestDto(
        @Pattern(regexp = "^[^\\\\:*?\"<>|&%$#@+={}\\[\\];'~`,№()]*$", message = "Path contains invalid characters")
        String path) {
}
