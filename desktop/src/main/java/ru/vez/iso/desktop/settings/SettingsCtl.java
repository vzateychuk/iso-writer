package ru.vez.iso.desktop.settings;

import javafx.event.ActionEvent;
import lombok.extern.java.Log;

@Log
public class SettingsCtl {

    private final SettingsSrv service;

    public SettingsCtl(SettingsSrv service) {
        this.service = service;
    }

    public void onSave(ActionEvent ev) {
        log.info("SettingsCtl.onSave");
    }

    public void onCancel(ActionEvent ev) {
        log.info("SettingsCtl.onCancel");
    }
}
