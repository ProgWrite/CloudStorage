package org.example.cloudstorage.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.dto.ErrorResponseDto;
import org.example.cloudstorage.exception.UserExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UserExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleUserAlreadyExists(UserExistsException exception){
        log.warn("USER ALREADY EXISTS");
        ErrorResponseDto error = buildError(exception);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationErrors(MethodArgumentNotValidException exception) {
        String errorMessage = buildValidationErrorMessage(exception);
        log.warn("Validation error: {}", errorMessage);
        ErrorResponseDto error = new ErrorResponseDto(errorMessage);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleAllExceptions(Exception ex) {
        log.error("Internal error: {}", ex.getMessage(), ex);
        ErrorResponseDto error = new ErrorResponseDto(
                "Oops! An unexpected error occurred.");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ErrorResponseDto buildError(RuntimeException exception){
        return new ErrorResponseDto(
                exception.getMessage()
        );
    }

    private String buildValidationErrorMessage(MethodArgumentNotValidException exception){
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
