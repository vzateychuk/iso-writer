package ru.vez.iso.desktop;

public enum ViewType {

    WELCOME("welcome.fxml", true),
    MAIN("main.fxml", true),
    LOGIN("login.fxml", true),
    SETTINGS("settings.fxml", true),
    DISK("disks.fxml", true),
    NAVIGATION("navigation.fxml", true);

    private final String fileName;
    private final boolean cacheable;

    ViewType(String fileName, boolean isCacheable) {
        this.fileName = fileName;
        this.cacheable = isCacheable;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isCacheable() {
        return cacheable;
    }
}
