package org.example.cloudstorage.mapper;

import io.minio.messages.Item;
import org.example.cloudstorage.dto.FileSystemItemResponseDto;
import org.example.cloudstorage.dto.ResourceType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import utils.PathUtils;


@Mapper
public interface FileSystemItemMapper {
    Long EMPTY_FOLDER_SIZE = 0L;

    FileSystemItemMapper INSTANCE = Mappers.getMapper(FileSystemItemMapper.class);

    //TODO может тут все изменится когда появятся файлы!!!
    @Mapping(target = "path", source = "path")
    @Mapping(target = "name", source = "item", qualifiedByName = "extractFolderName")
    @Mapping(target = "size", source = "item", qualifiedByName = "extractSizeByType")
    @Mapping(target = "type", source = "item", qualifiedByName = "determineResourceType")
    FileSystemItemResponseDto itemToDto(Item item, String path);

    @Named("extractFolderName")
    default String extractFolderName(Item item) {
        boolean isDirectory = item.objectName().endsWith("/");
        return PathUtils.extractFolderName(item.objectName(), isDirectory);
    }

    @Named("extractSizeByType")
    default Long extractSizeByType(Item item) {
        boolean isDirectory = item.objectName().endsWith("/");
        return isDirectory ? EMPTY_FOLDER_SIZE : item.size();
    }

    @Named("determineResourceType")
    default ResourceType determineResourceType(Item item) {
        boolean isDirectory = item.objectName().endsWith("/");
        return isDirectory ? ResourceType.DIRECTORY : ResourceType.FILE;
    }

}
