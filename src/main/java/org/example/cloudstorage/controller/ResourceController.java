package org.example.cloudstorage.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.FileSystemDeleteRequestDto;
import org.example.cloudstorage.dto.FileSystemItemRequestDto;
import org.example.cloudstorage.dto.FileSystemItemResponseDto;
import org.example.cloudstorage.model.User;
import org.example.cloudstorage.repository.UserRepository;
import org.example.cloudstorage.service.ResourceService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static utils.PathUtils.extractResourceName;


//TODO /resource можно переместить в RequestMapping
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
                                       @Valid FileSystemDeleteRequestDto file) {

        Optional<User> user = userRepository.findByUsername(userDetails.getUsername());
        Long id = user.get().getId();
        resourceService.delete(id, file.path());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/resource/download")
    public ResponseEntity<StreamingResponseBody> download(@Valid FileSystemItemRequestDto fileSystemDto,
                                                          @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        Optional<User> user = userRepository.findByUsername(userDetails.getUsername());
        Long id = user.get().getId();
        String resourceName = fileSystemDto.path();
        StreamingResponseBody responseBody = resourceService.download(id, resourceName);

        //TODO тут будет изменение кода, буду передавать взамен isTrailingSlash FILE OR FOLDER.
        boolean isTrailingSlash = false;
        String correctName = extractResourceName(resourceName, isTrailingSlash);

        String encodedName = URLEncoder.encode(correctName, StandardCharsets.UTF_8).replace("+", "%20");
        String contentDisposition = "attachment; filename*=utf-8''" + encodedName;
        String contentDispositionZip = "attachment; filename*=utf-8''" + encodedName + ".zip";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, resourceName.endsWith("/") ? contentDispositionZip : contentDisposition)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);

    }


}

