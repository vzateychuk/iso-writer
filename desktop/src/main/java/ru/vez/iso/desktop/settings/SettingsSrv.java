package ru.vez.iso.desktop.settings;

import ru.vez.iso.desktop.shared.AppSettings;

public interface SettingsSrv {

    AppSettings load(String filePath);

    AppSettings save(String filePath, AppSettings settings);

    void loadAsync(String filePath);

    void saveAsync(AppSettings sets);
}
