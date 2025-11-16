package org.example.cloudstorage.exception;

public class MinioOperationException extends RuntimeException {
    public MinioOperationException(String message) {
        super(message);
    }

    public MinioOperationException(String message, Throwable t) {
        super(message, t);
    }

    public MinioOperationException(Throwable t) {
        super(t);
    }

}
