package org.example.cloudstorage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthorizationRequestDto {

    @NotBlank(message = "Tne username can't be blank")
    private String username;

    @NotBlank(message = "Tne password can't be blank")
    private String password;
}
