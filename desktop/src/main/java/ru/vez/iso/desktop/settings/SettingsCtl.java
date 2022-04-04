package ru.vez.iso.desktop.settings;

import javafx.event.ActionEvent;

public class SettingsCtl {

    private final SettingsSrv service;

    public SettingsCtl(SettingsSrv service) {
        this.service = service;
    }

    public void onSave(ActionEvent ev) {
        System.out.println("SettingsCtl.onSave");
    }

    public void onCancel(ActionEvent ev) {
        System.out.println("SettingsCtl.onCancel");
    }
}
