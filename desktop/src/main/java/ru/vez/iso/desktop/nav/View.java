package ru.vez.iso.desktop.nav;

enum View {

    WELCOME("../welcome.fxml", true),
    MAIN("../main.fxml", true),
    LOGIN("../login.fxml", true),
    SETTINGS("../settings.fxml", true),
    DISK("../disks.fxml", true);

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
