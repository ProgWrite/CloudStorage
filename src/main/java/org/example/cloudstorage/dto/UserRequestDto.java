package org.example.cloudstorage.dto;

import lombok.*;

//TODO тут может будет другая структура
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto {
    private String username;
    private String password;
}
