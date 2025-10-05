package org.example.cloudstorage.controller;

//TODO может поменяю название контроллера

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RequestMapping("/api/user")
@RestController
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<Map<String,String>> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(Map.of("username", authentication.getName()));
    }
}