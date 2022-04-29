package ru.vez.iso.desktop.shared;


public enum SettingType {

    SETTING_FILE("settings.properties"),
    OPERATION_DAYS("30"),
    ISO_CACHE_PATH("./temp"),
    REFRESH_PERIOD("5"),
    ABDD_API("http://localhost:8080/abdd/api");

    private final String defaultValue;

    SettingType(String value) {
        this.defaultValue = value;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
