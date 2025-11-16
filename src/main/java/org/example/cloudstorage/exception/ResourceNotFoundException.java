package org.example.cloudstorage.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable t) {
        super(message, t);
    }

    public ResourceNotFoundException(Throwable t) {
        super(t);
    }

}
