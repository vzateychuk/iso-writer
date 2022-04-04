package ru.vez.iso.desktop.settings;

import javafx.event.ActionEvent;

public class SettingsCtl {

    private final SettingsService service;

    public SettingsCtl(SettingsService service) {
        this.service = service;
    }

    public void onSave(ActionEvent ev) {
        System.out.println("SettingsCtl.onSave");
    }

    public void onCancel(ActionEvent ev) {
        System.out.println("SettingsCtl.onCancel");
    }
}
