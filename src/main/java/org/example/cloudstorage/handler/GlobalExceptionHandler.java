package org.example.cloudstorage.handler;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.dto.ErrorResponseDto;
import org.example.cloudstorage.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UserExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleUserAlreadyExistsException(UserExistsException exception) {
        log.warn("USER ALREADY EXISTS");
        ErrorResponseDto error = buildError(exception);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFoundException(InternalAuthenticationServiceException exception) {
        log.warn("USER NOT FOUND");
        ErrorResponseDto error = buildError(exception);
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleWrongPasswordException(BadCredentialsException exception) {
        log.warn("WRONG PASSWORD");
        ErrorResponseDto error = buildError(exception);
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationErrors(MethodArgumentNotValidException exception) {
        String errorMessage = buildValidationErrorMessage(exception);
        log.warn("MoveOperationValidator error: {}", errorMessage);
        ErrorResponseDto error = new ErrorResponseDto(errorMessage);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidPathException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidPathException(InvalidPathException exception) {
        log.warn("INVALID PATH");
        ErrorResponseDto error = buildError(exception);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(ResourceNotFoundException exception) {
        log.warn("RESOURCE NOT FOUND");
        ErrorResponseDto error = buildError(exception);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponseDto> handleMultipartException(MultipartException exception) {
        log.warn("Request must contain a file");
        ErrorResponseDto error = buildError(exception);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceExistsException(ResourceExistsException exception) {
        log.warn("Resource already exists");
        ErrorResponseDto error = buildError(exception);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ApiResponse(
            description = "Internal server error",
            responseCode = "500",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDto.class)
            )
    )
    public ResponseEntity<ErrorResponseDto> handleAllExceptions(Exception ex) {
        log.error("Internal error: {}", ex.getMessage(), ex);
        ErrorResponseDto error = new ErrorResponseDto(
                "Oops! An unexpected error occurred.");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ErrorResponseDto buildError(RuntimeException exception) {
        return new ErrorResponseDto(
                exception.getMessage()
        );
    }

    private String buildValidationErrorMessage(MethodArgumentNotValidException exception) {
        BindingResult bindingResult = exception.getBindingResult();
        List<String> errors = new ArrayList<>();

        if (bindingResult.hasGlobalErrors()) {
            errors.addAll(bindingResult.getGlobalErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.toList()));
        }

        if (bindingResult.hasFieldErrors()) {
            errors.addAll(bindingResult.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList()));
        }
        return String.join(", ", errors);
    }

}
