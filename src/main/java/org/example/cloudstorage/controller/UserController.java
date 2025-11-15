package org.example.cloudstorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.apiDocs.UserApi;
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
public class UserController implements UserApi {

    @GetMapping("/me")
    public ResponseEntity<Map<String,String>> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(Map.of("username", authentication.getName()));
    }
}