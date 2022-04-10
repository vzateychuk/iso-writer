package ru.vez.iso.desktop.settings;

import java.util.Arrays;
import java.util.Properties;

public interface SettingsSrv {

    default Properties getDefaultPropsConfig() {
        Properties props = new Properties();
        Arrays.stream(SettingType.values()).forEach(
                t -> props.setProperty(t.name(), String.valueOf(t.getDefaultValue()))
        );
        return props;
    }

    void saveAsync(Properties props, String filePath);

    void loadAsync(String filePath);
}
