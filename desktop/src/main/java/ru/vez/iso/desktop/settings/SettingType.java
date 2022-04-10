package ru.vez.iso.desktop.settings;


public enum SettingType {

    SETTING_FILE("settings.properties"),
    OPERATION_DAYS("30"),
    REFRESH_PERIOD("5");

    private final String defaultValue;

    SettingType(String value) {
        this.defaultValue = value;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
