package org.example.cloudstorage.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthorizationRequestDto {
    private String username;
    private String password;
}
