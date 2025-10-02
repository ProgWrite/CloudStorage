package org.example.cloudstorage.dto;



import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
//TODO сделай валидацию!!!
public class UserRegistrationRequestDto {

    private String username;

    private String password;

    private String confirmPassword;
}
