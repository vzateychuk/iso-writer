package ru.vez.iso.desktop.document.reestr;

public enum ReestrFileType {

    PF("pdf"),
    JSON("json");

    private String ext;

    ReestrFileType(String ext) {
        this.ext = ext;
    }

    public String getExt() {
        return ext;
    }
}
