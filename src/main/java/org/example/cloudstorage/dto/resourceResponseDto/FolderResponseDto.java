package org.example.cloudstorage.dto.resourceResponseDto;

import org.example.cloudstorage.dto.ResourceType;

public record FolderResponseDto(
        String path,
        String name,
        ResourceType type) implements ResourceResponseDto {
}
