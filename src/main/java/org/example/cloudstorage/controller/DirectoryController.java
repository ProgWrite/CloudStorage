package org.example.cloudstorage.controller;


import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.DirectoryResponseDto;
import org.example.cloudstorage.model.User;
import org.example.cloudstorage.repository.UserRepository;
import org.example.cloudstorage.service.DirectoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DirectoryController {
    private final DirectoryService directoryService;
    private final UserRepository userRepository;

    @GetMapping("/directory")
    public ResponseEntity<?> getDirectory(@RequestParam String path,
                                          @AuthenticationPrincipal UserDetails userDetails){
        Optional<User> user  = userRepository.findByUsername(userDetails.getUsername());
        Long id = user.get().getId();
        List<DirectoryResponseDto > folder = directoryService.getDirectory(id, path);
        return ResponseEntity.ok(folder);
    }

    @PostMapping("/directory")
    public ResponseEntity<DirectoryResponseDto> createDirectory(@RequestParam String path,
                                                                @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> user  = userRepository.findByUsername(userDetails.getUsername());
        Long id = user.get().getId();
        DirectoryResponseDto folder = directoryService.createDirectory(id, path);
        return ResponseEntity.status(HttpStatus.CREATED).body(folder);
    }

}
