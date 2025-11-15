package org.example.cloudstorage.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.apiDocs.ResourceApi;
import org.example.cloudstorage.dto.fileSystemRequestDto.FileSystemPathRequestDto;
import org.example.cloudstorage.dto.fileSystemRequestDto.FileSystemMoveRequestDto;
import org.example.cloudstorage.dto.fileSystemRequestDto.FileSystemSearchRequestDto;
import org.example.cloudstorage.dto.resourceResponseDto.ResourceResponseDto;
import org.example.cloudstorage.service.ResourceService;
import org.example.cloudstorage.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.example.cloudstorage.utils.PathUtils.extractResourceName;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resource")
public class ResourceController implements ResourceApi {

    private final ResourceService resourceService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ResourceResponseDto> getResourceInfo(@Valid FileSystemPathRequestDto fileSystemDto,
                                                               @AuthenticationPrincipal UserDetails userDetails) {
        Long id = userService.getId(userDetails.getUsername());
        ResourceResponseDto resource = resourceService.getResourceInfo(id, fileSystemDto.path());
        return ResponseEntity.status(HttpStatus.OK).body(resource);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails userDetails,
                                       @Valid FileSystemPathRequestDto fileSystemDto) {

        Long id = userService.getId(userDetails.getUsername());
        resourceService.delete(id, fileSystemDto.path());
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<List<ResourceResponseDto>> upload(@Valid FileSystemPathRequestDto fileSystemDto,
                                                            @RequestPart("object") MultipartFile[] file,
                                                            @AuthenticationPrincipal UserDetails userDetails) {
        Long id = userService.getId(userDetails.getUsername());
        List<ResourceResponseDto> resources = resourceService.upload(id, fileSystemDto.path(), file);
        return ResponseEntity.status(HttpStatus.CREATED).body(resources);
    }

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> download(@Valid FileSystemPathRequestDto fileSystemDto,
                                                          @AuthenticationPrincipal UserDetails userDetails) {

        Long id = userService.getId(userDetails.getUsername());
        String resourceName = fileSystemDto.path();
        StreamingResponseBody responseBody = resourceService.download(id, resourceName);

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

    @GetMapping("/move")
    public ResponseEntity<ResourceResponseDto> move(@Valid FileSystemMoveRequestDto fileMoveDto,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        Long id = userService.getId(userDetails.getUsername());
        ResourceResponseDto resource = resourceService.move(id, fileMoveDto.from(), fileMoveDto.to());

        return ResponseEntity.ok(resource);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResourceResponseDto>> search(@Valid FileSystemSearchRequestDto dto,
                                                            @AuthenticationPrincipal UserDetails userDetails) {
        Long id = userService.getId(userDetails.getUsername());
        List<ResourceResponseDto> queryResults = resourceService.search(id, dto.query());
        return ResponseEntity.status(HttpStatus.OK).body(queryResults);
    }

}

