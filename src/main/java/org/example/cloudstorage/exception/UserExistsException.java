package org.example.cloudstorage.exception;

public class UserExistsException extends RuntimeException {
    public UserExistsException(String message) {
        super(message);
    }

    public UserExistsException(String message, Throwable t) {
        super(message, t);
    }

    public UserExistsException(Throwable t) {
        super(t);
    }


}
