package ru.vez.iso.desktop.settings;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import lombok.extern.java.Log;

@Log
public class SettingsCtl {

    @FXML private ToggleGroup opsPeriodGroup;
    @FXML private RadioButton customPeriod;
    @FXML private RadioButton monthPeriod;
    @FXML private DatePicker opsDaysFrom;
    @FXML private DatePicker opsDaysTo;
    @FXML private RadioButton quarterPeriod;
    @FXML private TextField refreshPeriod;
    @FXML private RadioButton weekPeriod;

    private final SettingsSrv service;

    public SettingsCtl(SettingsSrv service) {
        this.service = service;
    }

    @FXML public void onSave(ActionEvent ev) {
        log.info("SettingsCtl.onSave");
    }

    @FXML public void onCancel(ActionEvent ev) {
        log.info("SettingsCtl.onCancel");
    }
}
