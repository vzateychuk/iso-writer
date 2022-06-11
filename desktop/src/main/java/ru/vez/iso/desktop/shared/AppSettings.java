package ru.vez.iso.desktop.shared;

import lombok.Builder;
import lombok.Getter;

import java.util.Properties;

/**
 * Дата модель для хранения и передачи настроек
 * */
@Getter
@Builder
public class AppSettings {

    private final String settingFile;
    private final int refreshMin;
    private final int filterOpsDays;
    private final String isoCachePath;
    private final String backendAPI;

    public Properties getProperties() {

        Properties p = new Properties();
        p.setProperty(SettingType.REFRESH_PERIOD.name(), String.valueOf(this.refreshMin));
        p.setProperty(SettingType.SETTING_FILE.name(), this.settingFile);
        p.setProperty(SettingType.OPERATION_DAYS.name(), String.valueOf(this.filterOpsDays));
        p.setProperty(SettingType.ISO_CACHE_PATH.name(), this.isoCachePath);
        p.setProperty(SettingType.BACKEND_API.name(), this.backendAPI);
        return p;
    }
}
