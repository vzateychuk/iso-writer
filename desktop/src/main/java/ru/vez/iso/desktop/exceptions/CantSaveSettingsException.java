package ru.vez.iso.desktop.exceptions;

public class CantSaveSettingsException extends RuntimeException {

    public CantSaveSettingsException(Exception e) {
        super(e);
    }
}
