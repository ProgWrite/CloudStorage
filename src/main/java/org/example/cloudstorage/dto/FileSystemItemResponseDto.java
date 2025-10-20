package org.example.cloudstorage.dto;

public record FileSystemItemResponseDto(
        String path,
        String name,
        Long size,
        ResourceType type) {
}
