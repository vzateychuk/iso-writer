package ru.vez.iso.desktop.exceptions;

public class HttpNotAuthorizedException extends RuntimeException {

    public HttpNotAuthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpNotAuthorizedException(Exception e) {
        super(e);
    }

    public HttpNotAuthorizedException(String s) {
        super(s);
    }
}
