package org.example.cloudstorage.dto;

//TODO возможно какую-либо валидацию!!!

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record FileSystemSearchRequestDto(

        @NotBlank(message = "Search query cannot be empty")
        @Size(min = 1, max = 30, message = "Search query must be between 1 and 30 characters")
        @Pattern(regexp = "^[^/\\\\:*?\"<>|]*$" , message = "Search query cannot contain path symbols: / \\\\ : * ? \\\" < > |")
        String query) {
}
