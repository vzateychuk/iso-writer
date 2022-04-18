package ru.vez.iso.desktop.document;

/**
 * Статус документа
 * */
public enum DocStatus {
    UNKNOWN_STATE("Неизвестный статус");

    private final String title;

    DocStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
