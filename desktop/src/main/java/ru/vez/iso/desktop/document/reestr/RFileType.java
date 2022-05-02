package ru.vez.iso.desktop.document.reestr;

public enum RFileType {

    ED("электронный документ"),
    PF("печатная форма"),
    JSON("Исходный JSON-файл"),
    ISO("ISO образ");

    private String title;

    RFileType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
