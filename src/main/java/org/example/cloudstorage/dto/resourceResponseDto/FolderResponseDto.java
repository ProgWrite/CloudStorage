package org.example.cloudstorage.dto.resourceResponseDto;

import org.example.cloudstorage.model.ResourceType;

public record FolderResponseDto(
        String path,
        String name,
        ResourceType type) implements ResourceResponseDto {
}
