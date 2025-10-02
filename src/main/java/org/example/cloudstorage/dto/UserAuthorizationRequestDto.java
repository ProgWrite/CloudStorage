package org.example.cloudstorage.dto;

import lombok.*;

//TODO тут может будет другая структура + нужна какая-то валидация)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthorizationRequestDto {
    private String username;
    private String password;
}
