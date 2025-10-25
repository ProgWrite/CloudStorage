package org.example.cloudstorage.dto;

import jakarta.validation.constraints.Pattern;

//TODO В ДАЛЬНЙШЕМ МОЖНО ВВЕСТИ АННОТАЦИЮ SIZE когда решу какое максимальное по символам название может иметь папка.
public record FileSystemItemRequestDto(

        @Pattern(regexp = "^[^\\\\:*?\"<>|&%$#!@+={}\\[\\];'~`,№()\\-]*$", message = "Path contains invalid characters")
        String path) {
}
