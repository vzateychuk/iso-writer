package ru.vez.iso.desktop.docs;

/**
 * Статус документа
 * */
public enum DocStatus {
    CREATED("Создан"),
    UNKNOWN_STATE("Неизвестный статус");

    private final String title;

    DocStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
