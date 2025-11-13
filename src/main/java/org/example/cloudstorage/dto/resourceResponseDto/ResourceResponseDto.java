package org.example.cloudstorage.dto.resourceResponseDto;

import org.example.cloudstorage.model.ResourceType;

public sealed interface ResourceResponseDto
        permits FileResponseDto, FolderResponseDto {

    String path();
    String name();
    ResourceType type();

}
