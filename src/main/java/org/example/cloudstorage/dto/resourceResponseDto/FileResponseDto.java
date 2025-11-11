package org.example.cloudstorage.dto.resourceResponseDto;

import org.example.cloudstorage.dto.ResourceType;

public record FileResponseDto(
        String path,
        String name,
        Long size,
        ResourceType type) implements ResourceResponseDto {
}
