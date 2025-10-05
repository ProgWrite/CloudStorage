package org.example.cloudstorage.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.dto.UserAuthorizationRequestDto;
import org.example.cloudstorage.dto.UserRegistrationRequestDto;
import org.example.cloudstorage.dto.UserResponseDto;
import org.example.cloudstorage.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final SecurityContextRepository securityContextRepository;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    //TODO Этот метод надо будет улучшить (пересмотреть возможно)!
    @PostMapping("/sign-in")
    public ResponseEntity<UserResponseDto> signIn(@RequestBody UserAuthorizationRequestDto user, HttpServletRequest request,
                                                  HttpServletResponse response) {
        log.info("Attempting authentication for user: {}", user.getUsername());
        authenticateUser(user.getUsername(), user.getPassword(), request, response);
        UserResponseDto responseDto = new UserResponseDto(user.getUsername());
        return ResponseEntity.ok(responseDto);
    }


    @PostMapping("/sign-up")
    public ResponseEntity<UserResponseDto> signUp(@Valid @RequestBody UserRegistrationRequestDto user, HttpServletRequest request,
                                                  HttpServletResponse response) {
        log.info("Attempting registration for user: {}", user.getUsername());
        UserResponseDto responseDto =    userService.create(user);
        authenticateUser(user.getUsername(), user.getPassword(), request, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    private void authenticateUser(String username, String password, HttpServletRequest request,
                                  HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }

}
