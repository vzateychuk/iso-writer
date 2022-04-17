package ru.vez.iso.desktop.document;

/**
 * Статус документа
 * */
public enum DocStatus {
    MAIN_STATUS("Статус документа");

    private final String title;

    DocStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
