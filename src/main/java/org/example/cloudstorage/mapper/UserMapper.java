package org.example.cloudstorage.mapper;

import org.example.cloudstorage.dto.userDto.UserRegistrationRequestDto;
import org.example.cloudstorage.dto.userDto.UserResponseDto;
import org.example.cloudstorage.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {


    @Mapping(target = "id", ignore = true)
    User toEntity(UserRegistrationRequestDto userRegistrationRequestDto);

    UserResponseDto toResponseDto(User user);

    default org.springframework.security.core.userdetails.User toUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                java.util.Collections.emptyList()
        );
    }


}
