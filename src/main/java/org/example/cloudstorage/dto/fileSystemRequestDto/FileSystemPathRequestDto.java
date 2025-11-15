package org.example.cloudstorage.dto.fileSystemRequestDto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.RequestParam;


public record FileSystemPathRequestDto(

        @Parameter(description = "Path to the target directory for upload")
        @Pattern(regexp = "^[^\\\\:*?\"<>|]*$", message = "Path contains invalid characters")
        String path) {
}
