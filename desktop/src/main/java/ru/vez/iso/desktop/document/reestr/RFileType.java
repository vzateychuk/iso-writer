package ru.vez.iso.desktop.document.reestr;

public enum RFileType {

    ED("электронный документ"),
    PF("печатная форма"),
    JSON("Исходный JSON-файл"),
    ISO("ISO образ");

    private String ext;

    RFileType(String ext) {
        this.ext = ext;
    }

    public String getExt() {
        return ext;
    }
}
