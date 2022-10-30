package ru.vez.iso.desktop.exceptions;

import org.apache.http.HttpStatus;

public class ExceptionHelper {

    private ExceptionHelper() { }


    public static void handleHttExceptionStatus(int statusCode) {

        if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
            throw new HttpNotAuthorizedException("Server response: " + statusCode);
        } else {
            throw new HttpRequestException("Bad server response: " + statusCode);
        }
    }
}
