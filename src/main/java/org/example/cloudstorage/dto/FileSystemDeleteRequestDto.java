package org.example.cloudstorage.dto;

import jakarta.validation.constraints.Pattern;

//TODO может не нужен будет этот класс, если паттерны одинаковы. но может и нужен, так как есть метод в сервисе isPathValidToDelete. Может я сделаю его кастомной аннотацией.
public record FileSystemDeleteRequestDto(


        @Pattern(regexp = "^[^\\\\:*?\"<>|&%$#!@+={}\\[\\];'~`,№()]*$", message = "Path contains invalid characters")
        String path) {
}
