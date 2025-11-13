package org.example.cloudstorage.dto.userDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
