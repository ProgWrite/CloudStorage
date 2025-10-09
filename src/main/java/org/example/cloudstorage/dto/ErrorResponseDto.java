package org.example.cloudstorage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponseDto(
        @Schema(description = "Error message")
        String message) {
}
