package org.example.cloudstorage.dto.resourceResponseDto;

import org.example.cloudstorage.dto.ResourceType;

public sealed interface ResourceResponseDto
        permits FileResponseDto, FolderResponseDto {

    String path();
    String name();
    ResourceType type();

}
