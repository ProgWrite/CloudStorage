package org.example.cloudstorage.controller;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.FileSystemItemResponseDto;
import org.example.cloudstorage.model.User;
import org.example.cloudstorage.repository.UserRepository;
import org.example.cloudstorage.service.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ResourceController {
    private final ResourceService resourceService;
    private final UserRepository userRepository;

    @PostMapping("/resource")
    public ResponseEntity<List<FileSystemItemResponseDto>>uploadResource(@RequestParam String path,
                                                                         @RequestPart("object") MultipartFile[] file,
                                                                         @AuthenticationPrincipal UserDetails userDetails){
        Optional<User> user  = userRepository.findByUsername(userDetails.getUsername());
        Long id = user.get().getId();
        List<FileSystemItemResponseDto> filesDto = resourceService.upload(id, path, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(filesDto);
    }

}
