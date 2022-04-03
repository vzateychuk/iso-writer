package ru.vez.iso.desktop;

public enum View {

    LOGIN("login.fxml", true),
    MAIN("main.fxml", true),
    SETTINGS("settings.fxml", false);

    private final String fileName;
    private final boolean cacheable;

    View(String fileName, boolean isCacheable) {
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
