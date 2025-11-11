package org.example.cloudstorage.mapper;

import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import org.example.cloudstorage.dto.resourceResponseDto.FileResponseDto;
import org.example.cloudstorage.dto.resourceResponseDto.ResourceResponseDto;
import org.example.cloudstorage.dto.resourceResponseDto.FolderResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import utils.PathUtils;

import static utils.PathUtils.deleteRootPath;


@Mapper
public interface FileSystemItemMapper {

    FileSystemItemMapper INSTANCE = Mappers.getMapper(FileSystemItemMapper.class);

    default ResourceResponseDto itemToDto(Item item, String path) {
        boolean isDirectory = item.objectName().endsWith("/");

        if (isDirectory) {
            return itemToFolderDto(item, path);
        } else {
            return itemToFileDto(item, path);
        }
    }

    default ResourceResponseDto statObjectToDto(StatObjectResponse object, Long userId) {
        String fullName = object.object();
        String relativePath = deleteRootPath(fullName, userId);
        String path = PathUtils.buildParentPath(relativePath);
        String resourceName = PathUtils.extractResourceName(fullName, false);

        if (fullName.endsWith("/")) {
            return statToFolderDto(object, path, resourceName);
        } else {
            return statToFileDto(object, path, resourceName);
        }
    }

    @Mapping(target = "path", source = "path")
    @Mapping(target = "name", source = "resourceName")
    @Mapping(target = "type", expression = "java(ResourceType.DIRECTORY)")
    FolderResponseDto statToFolderDto(StatObjectResponse object, String path, String resourceName);

    @Mapping(target = "path", source = "path")
    @Mapping(target = "name", source = "resourceName")
    @Mapping(target = "size", expression = "java(object.size())")
    @Mapping(target = "type", expression = "java(ResourceType.FILE)")
    FileResponseDto statToFileDto(StatObjectResponse object, String path, String resourceName);


    @Mapping(target = "path", source = "path")
    @Mapping(target = "name", source = "item", qualifiedByName = "extractResourceName")
    @Mapping(target = "type", expression = "java(ResourceType.DIRECTORY)")
    FolderResponseDto itemToFolderDto(Item item, String path);

    @Mapping(target = "path", source = "path")
    @Mapping(target = "name", source = "item", qualifiedByName = "extractResourceName")
    @Mapping(target = "size", source = "item", qualifiedByName = "extractSize")
    @Mapping(target = "type", expression = "java(ResourceType.FILE)")
    FileResponseDto itemToFileDto(Item item, String path);

    @Named("extractResourceName")
    default String extractResourceName(Item item) {
        boolean isDirectory = item.objectName().endsWith("/");
        return PathUtils.extractResourceName(item.objectName(), isDirectory);
    }

    @Named("extractSize")
    default Long extractSize(Item item) {
        return item.size();
    }


}
