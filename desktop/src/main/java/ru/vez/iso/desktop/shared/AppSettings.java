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
    private final int evictCacheDays;
    private final int burnSpeed;
    private final String authAPI;
    private final String authPath;
    // KeyCloak authentication params
    private final String clientId;
    private final String grantType;
    private final String clientSecret;

    public Properties getProperties() {

        Properties p = new Properties();
        p.setProperty(SettingType.REFRESH_PERIOD.name(), String.valueOf(this.refreshMin));
        p.setProperty(SettingType.SETTING_FILE.name(), this.settingFile);
        p.setProperty(SettingType.OPERATION_DAYS.name(), String.valueOf(this.filterOpsDays));
        p.setProperty(SettingType.ISO_CACHE_PATH.name(), this.isoCachePath);
        p.setProperty(SettingType.BACKEND_API.name(), this.backendAPI);
        p.setProperty(SettingType.EVICT_CACHE_DAYS.name(), String.valueOf(this.evictCacheDays));
        p.setProperty(SettingType.BURN_SPEED.name(), String.valueOf(this.burnSpeed));
        p.setProperty(SettingType.AUTH_API.name(), String.valueOf(this.authAPI));
        p.setProperty(SettingType.AUTH_PATH.name(), String.valueOf(this.authPath));
        // KeyCloak authentication
        p.setProperty(SettingType.CLIENT_ID.name(), String.valueOf(this.clientId));
        p.setProperty(SettingType.GRANT_TYPE.name(), String.valueOf(this.grantType));
        p.setProperty(SettingType.CLIENT_SECRET.name(), String.valueOf(this.clientSecret));
        return p;
    }
}
