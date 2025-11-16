package org.example.cloudstorage.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.apiDocs.DirectoryApi;
import org.example.cloudstorage.dto.fileSystemRequestDto.FileSystemPathRequestDto;
import org.example.cloudstorage.dto.resourceResponseDto.FolderResponseDto;
import org.example.cloudstorage.dto.resourceResponseDto.ResourceResponseDto;
import org.example.cloudstorage.model.TraversalMode;
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

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DirectoryController implements DirectoryApi {

    private final DirectoryService directoryService;
    private final UserService userService;


    @GetMapping("/directory")
    public ResponseEntity<List<ResourceResponseDto>> getDirectory(@Valid FileSystemPathRequestDto fileSystemDto,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Getting directory for user: {}, path: {}", userDetails.getUsername(), fileSystemDto.path());

        Long id = userService.getId(userDetails.getUsername());
        List<ResourceResponseDto> resource = directoryService.getDirectory(id, fileSystemDto.path(), TraversalMode.NON_RECURSIVE);

        log.info("Successfully retrieved items from directory: {}", fileSystemDto.path());

        return ResponseEntity.ok(resource);
    }

    @PostMapping("/directory")
    public ResponseEntity<FolderResponseDto> createDirectory(@Valid FileSystemPathRequestDto fileSystemDto,
                                                             @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Creating directory for user: {}, path: {}", userDetails.getUsername(), fileSystemDto.path());

        Long id = userService.getId(userDetails.getUsername());
        FolderResponseDto folder = directoryService.createDirectory(id, fileSystemDto.path());

        log.info("Directory created successfully for user: {}, path: {}", userDetails.getUsername(), fileSystemDto.path());

        return ResponseEntity.status(HttpStatus.CREATED).body(folder);
    }

}
