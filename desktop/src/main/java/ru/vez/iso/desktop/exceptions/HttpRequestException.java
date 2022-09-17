package ru.vez.iso.desktop.exceptions;

public class HttpRequestException extends RuntimeException {

    public HttpRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpRequestException(Exception e) {
        super(e);
    }

    public HttpRequestException(String s) {
        super(s);
    }
}
