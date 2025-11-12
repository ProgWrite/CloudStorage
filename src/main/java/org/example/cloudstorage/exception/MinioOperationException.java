package org.example.cloudstorage.exception;

public class MinioOperationException extends RuntimeException {
    public MinioOperationException(String message) {
        super(message);
    }
}
