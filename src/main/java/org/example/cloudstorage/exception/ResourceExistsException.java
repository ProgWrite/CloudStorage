package org.example.cloudstorage.exception;

public class ResourceExistsException extends RuntimeException {
    public ResourceExistsException(String message) {
        super(message);
    }

    public ResourceExistsException(String message, Throwable t) {
        super(message, t);
    }

    public ResourceExistsException(Throwable t) {
        super(t);
    }
}
