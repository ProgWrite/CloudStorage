package org.example.cloudstorage.exception;

public class FileDownloadException extends RuntimeException {
    public FileDownloadException(String message) {
        super(message);
    }

    public FileDownloadException(String message, Throwable t) {
        super(message, t);
    }

    public FileDownloadException(Throwable t) {
        super(t);
    }

}
