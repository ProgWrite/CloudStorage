package org.example.cloudstorage.dto;


public record UserResponseDto(
        Long id,
        String username) {

    //TODO id может быть очень опасным. Подумай как избавиться от этого
    public UserResponseDto(String username) {
        this(null, username);
    }
}
