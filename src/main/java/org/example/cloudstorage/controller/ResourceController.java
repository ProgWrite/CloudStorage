package org.example.cloudstorage.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.FileSystemDeleteRequestDto;
import org.example.cloudstorage.dto.FileSystemItemRequestDto;
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

    @GetMapping("/resource")
    public ResponseEntity<FileSystemItemResponseDto> getResourceInfo(@Valid FileSystemItemRequestDto fileSystemDto,
                                                                     @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> user = userRepository.findByUsername(userDetails.getUsername());
        Long id = user.get().getId();
        FileSystemItemResponseDto resource = resourceService.getResourceInfo(id, fileSystemDto.path());
        return ResponseEntity.status(HttpStatus.OK).body(resource);
    }

    //TODO первые 2 строки кода повторяются везде. Из-за того что приходится вытаскивать id. Подумай как оптимизировать это.
    @PostMapping("/resource")
    public ResponseEntity<List<FileSystemItemResponseDto>> uploadResource(@Valid FileSystemItemRequestDto fileSystemDto,
                                                                          @RequestPart("object") MultipartFile[] file,
                                                                          @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> user = userRepository.findByUsername(userDetails.getUsername());
        Long id = user.get().getId();
        List<FileSystemItemResponseDto> filesDto = resourceService.upload(id, fileSystemDto.path(), file);
        return ResponseEntity.status(HttpStatus.CREATED).body(filesDto);
    }

    @DeleteMapping("/resource")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails userDetails,
                                       @Valid FileSystemDeleteRequestDto file){

        Optional<User> user = userRepository.findByUsername(userDetails.getUsername());
        Long id = user.get().getId();
        resourceService.delete(id, file.path());
        return ResponseEntity.noContent().build();
    }




}
