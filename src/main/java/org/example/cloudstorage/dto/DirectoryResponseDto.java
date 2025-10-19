package org.example.cloudstorage.dto;

//TODO возможно здесь будет дто и для папок и для файлов. Посмотрим в дальнейшем
public record DirectoryResponseDto(
        String path,
        String name,
        Long size,
        ResourceType type) {
}
