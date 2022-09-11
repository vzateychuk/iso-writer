package ru.vez.iso.desktop.main.exceptions;

public class FileCacheException extends RuntimeException {

    public FileCacheException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileCacheException(Exception e) {
        super(e);
    }
}
