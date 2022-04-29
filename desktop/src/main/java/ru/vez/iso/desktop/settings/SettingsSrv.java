package ru.vez.iso.desktop.settings;

import ru.vez.iso.desktop.shared.AppSettings;

public interface SettingsSrv {

    void loadAsync(String filePath);

    void saveAsync(AppSettings sets);
}
