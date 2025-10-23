package org.example.cloudstorage.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.FileSystemItemRequestDto;
import org.example.cloudstorage.dto.FileSystemItemResponseDto;
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
    public ResponseEntity<?> getDirectory(@Valid FileSystemItemRequestDto fileSystemDto,
                                          @AuthenticationPrincipal UserDetails userDetails){
        Optional<User> user  = userRepository.findByUsername(userDetails.getUsername());
        Long id = user.get().getId();
        List<FileSystemItemResponseDto> folder = directoryService.getDirectory(id, fileSystemDto.path());
        return ResponseEntity.ok(folder);
    }

    @PostMapping("/directory")
    public ResponseEntity<FileSystemItemResponseDto> createDirectory(@Valid FileSystemItemRequestDto fileSystemDto,
                                                                     @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> user  = userRepository.findByUsername(userDetails.getUsername());
        Long id = user.get().getId();
        FileSystemItemResponseDto folder = directoryService.createDirectory(id, fileSystemDto.path());
        return ResponseEntity.status(HttpStatus.CREATED).body(folder);
    }

}
