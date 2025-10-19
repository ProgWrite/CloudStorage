package org.example.cloudstorage.dto;

//TODO возможно здесь будет дто и для папок и для файлов. Посмотрим в дальнейшем
// может сделать чтобы для папок было 3 поля, а для файлов 4 поля. Если можно.
public record FileSystemItemResponseDto(
        String path,
        String name,
        Long size,
        ResourceType type) {
}
