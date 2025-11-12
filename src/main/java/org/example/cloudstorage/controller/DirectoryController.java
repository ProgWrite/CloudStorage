package org.example.cloudstorage.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.FileSystemItemRequestDto;
import org.example.cloudstorage.dto.resourceResponseDto.FolderResponseDto;
import org.example.cloudstorage.dto.resourceResponseDto.ResourceResponseDto;
import org.example.cloudstorage.service.DirectoryService;
import org.example.cloudstorage.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import utils.TraversalMode;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DirectoryController {

    private final DirectoryService directoryService;
    private final UserService userService;


    @GetMapping("/directory")
    public ResponseEntity<?> getDirectory(@Valid FileSystemItemRequestDto fileSystemDto,
                                          @AuthenticationPrincipal UserDetails userDetails) {

        Long id = userService.getId(userDetails.getUsername());
        List<ResourceResponseDto> folder = directoryService.getDirectory(id, fileSystemDto.path(), TraversalMode.NON_RECURSIVE);
        return ResponseEntity.ok(folder);
    }

    @PostMapping("/directory")
    public ResponseEntity<FolderResponseDto> createDirectory(@Valid FileSystemItemRequestDto fileSystemDto,
                                                             @AuthenticationPrincipal UserDetails userDetails) {

        Long id = userService.getId(userDetails.getUsername());
        FolderResponseDto folder = directoryService.createDirectory(id, fileSystemDto.path());
        return ResponseEntity.status(HttpStatus.CREATED).body(folder);
    }

}
