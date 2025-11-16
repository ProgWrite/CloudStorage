package org.example.cloudstorage.exception;

public class InvalidPathException extends RuntimeException {
    public InvalidPathException(String message) {
        super(message);
    }

    public InvalidPathException(String message, Throwable t) {
        super(message, t);
    }

    public InvalidPathException(Throwable t) {
        super(t);
    }

}
