package org.example.cloudstorage.exception;

public class UserRegistrationException extends RuntimeException {

  public UserRegistrationException(String message) {
        super(message);
    }

  public UserRegistrationException(String message, Throwable t) {
    super(message, t);
  }

  public UserRegistrationException(Throwable t) {
    super(t);
  }

}
