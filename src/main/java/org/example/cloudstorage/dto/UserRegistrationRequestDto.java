package org.example.cloudstorage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.example.cloudstorage.validation.PasswordMatches;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
//TODO эта аннотация вроде мешает работать фронтенду. Пока что не знаю почему.
@PasswordMatches
public class UserRegistrationRequestDto {

    @NotBlank(message = "Tne username can't be blank")
    @Size(min = 5, max = 20, message = "The username must be between 5 and 20 characters long")
    @Pattern(regexp = "^[a-zA-Z0-9]+[a-zA-Z_0-9]*[a-zA-Z0-9]+$", message = "The username must start and end with english letter or digit, may contain underscores inside")
    private String username;

    @NotBlank(message = "Tne password can't be blank")
    @Size(min = 5, max = 20, message = "The password must be between 5 and 20 characters long")
    @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*(),.?\":{}|<>\\[\\]/`~+=';_\\-]*$",
            message = "The password must contain only allowed characters: letters, digits and special symbols")
    private String password;

    private String confirmPassword;
}
