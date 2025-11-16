package org.example.cloudstorage.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.apiDocs.ResourceApi;
import org.example.cloudstorage.dto.fileSystemRequestDto.FileSystemMoveRequestDto;
import org.example.cloudstorage.dto.fileSystemRequestDto.FileSystemPathRequestDto;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resource")
public class ResourceController implements ResourceApi {

    private final ResourceService resourceService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ResourceResponseDto> getResourceInfo(@Valid FileSystemPathRequestDto fileSystemDto,
                                                               @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Getting resource info - user: {}, path: {}", userDetails.getUsername(), fileSystemDto.path());

        Long id = userService.getId(userDetails.getUsername());
        ResourceResponseDto resource = resourceService.getResourceInfo(id, fileSystemDto.path());

        log.debug("Resource info retrieved - user: {}, path: {}, type: {}",
                userDetails.getUsername(), fileSystemDto.path(), resource.type());

        return ResponseEntity.status(HttpStatus.OK).body(resource);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails userDetails,
                                       @Valid FileSystemPathRequestDto fileSystemDto) {

        log.warn("Deleting resource - user: {}, path: {}", userDetails.getUsername(), fileSystemDto.path());

        Long id = userService.getId(userDetails.getUsername());
        resourceService.delete(id, fileSystemDto.path());

        log.warn("Resource deleted - user: {}, path: {}", userDetails.getUsername(), fileSystemDto.path());

        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<List<ResourceResponseDto>> upload(@Valid FileSystemPathRequestDto fileSystemDto,
                                                            @RequestPart("object") MultipartFile[] file,
                                                            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Uploading files - user: {}, target path: {}, files count: {}",
                userDetails.getUsername(), fileSystemDto.path(), file.length);

        Long id = userService.getId(userDetails.getUsername());
        List<ResourceResponseDto> resources = resourceService.upload(id, fileSystemDto.path(), file);

        log.info("Files uploaded successfully - user: {}, path: {}, uploaded resources: {}",
                userDetails.getUsername(), fileSystemDto.path(), resources.size());

        return ResponseEntity.status(HttpStatus.CREATED).body(resources);
    }

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> download(@Valid FileSystemPathRequestDto fileSystemDto,
                                                          @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Downloading resource - user: {}, path: {}", userDetails.getUsername(), fileSystemDto.path());

        Long id = userService.getId(userDetails.getUsername());
        String resourceName = fileSystemDto.path();
        StreamingResponseBody responseBody = resourceService.download(id, resourceName);

        boolean isTrailingSlash = false;
        String correctName = extractResourceName(resourceName, isTrailingSlash);

        String encodedName = URLEncoder.encode(correctName, StandardCharsets.UTF_8).replace("+", "%20");
        String contentDisposition = "attachment; filename*=utf-8''" + encodedName;
        String contentDispositionZip = "attachment; filename*=utf-8''" + encodedName + ".zip";

        log.debug("Downloaded successfully for user: {}, path: {}",
                userDetails.getUsername(), resourceName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, resourceName.endsWith("/") ? contentDispositionZip : contentDisposition)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);

    }

    @GetMapping("/move")
    public ResponseEntity<ResourceResponseDto> move(@Valid FileSystemMoveRequestDto fileMoveDto,
                                                    @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Moving resource - user: {}, from: {}, to: {}",
                userDetails.getUsername(), fileMoveDto.from(), fileMoveDto.to());

        Long id = userService.getId(userDetails.getUsername());
        ResourceResponseDto resource = resourceService.move(id, fileMoveDto.from(), fileMoveDto.to());

        log.info("Resource moved successfully - user: {}, from: {}, to: {}",
                userDetails.getUsername(), fileMoveDto.from(), fileMoveDto.to());

        return ResponseEntity.ok(resource);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResourceResponseDto>> search(@Valid FileSystemSearchRequestDto dto,
                                                            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Searching resources - user: {}, query: {}", userDetails.getUsername(), dto.query());

        Long id = userService.getId(userDetails.getUsername());
        List<ResourceResponseDto> queryResults = resourceService.search(id, dto.query());

        log.info("Search completed - user: {}, query: {}, results: {}",
                userDetails.getUsername(), dto.query(), queryResults.size());

        return ResponseEntity.status(HttpStatus.OK).body(queryResults);
    }

}

