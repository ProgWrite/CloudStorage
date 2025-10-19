package org.example.cloudstorage.mapper;

import io.minio.messages.Item;
import org.example.cloudstorage.dto.DirectoryResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import utils.PathUtils;

@Mapper
public interface DirectoryMapper {

    DirectoryMapper INSTANCE = Mappers.getMapper(DirectoryMapper.class);

    //TODO может тут все изменится когда появятся файлы!!!
    @Mapping(target = "path", source = "path")
    @Mapping(target = "name", source = "item", qualifiedByName = "extractFolderName")
    @Mapping(target = "size", ignore = true)
    @Mapping(target = "type", constant = "DIRECTORY")
    DirectoryResponseDto itemToDto(Item item, String path);

    @Named("extractFolderName")
    default String extractFolderName(Item item) {
        return PathUtils.extractFolderName(item.objectName(), true);
    }


}
