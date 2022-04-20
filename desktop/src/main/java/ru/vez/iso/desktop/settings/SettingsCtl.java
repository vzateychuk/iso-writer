package ru.vez.iso.desktop.settings;

import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import lombok.extern.java.Log;
import ru.vez.iso.desktop.state.AppStateData;
import ru.vez.iso.desktop.state.AppStateType;

import java.util.Properties;

/**
 * Controller for View "Форма настроек"
 * */
@Log
public class SettingsCtl {

    @FXML private ToggleGroup opsPeriodGroup;
    @FXML private RadioButton quarterPeriod;
    @FXML private RadioButton monthPeriod;
    @FXML private RadioButton weekPeriod;
    @FXML private RadioButton customPeriod;
    @FXML private TextField operationDays;
    @FXML private TextField refreshPeriod;

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final SettingsSrv service;

    public SettingsCtl(ObservableMap<AppStateType, AppStateData> appState, SettingsSrv service) {
        this.appState = appState;
        this.service = service;
        this.appState.addListener(
                (MapChangeListener<AppStateType, AppStateData>) change -> {
                    if (AppStateType.SETTINGS.equals(change.getKey())) {
                        Platform.runLater(()->displaySettings((Properties)change.getValueAdded().getValue()));
                    }
                });
    }

    @FXML public void onSave(ActionEvent ev) {
        Properties props = ((AppStateData<Properties>)appState.get(AppStateType.SETTINGS)).getValue();
        int operDays = parseIntOrDefault(operationDays.getText(), SettingType.OPERATION_DAYS.getDefaultValue());
        props.setProperty(SettingType.OPERATION_DAYS.name(), String.valueOf(operDays));
        int refreshPrd = parseIntOrDefault(refreshPeriod.getText(), SettingType.REFRESH_PERIOD.getDefaultValue());
        props.setProperty(SettingType.REFRESH_PERIOD.name(), String.valueOf(refreshPrd));
        service.saveAsync(props, SettingType.SETTING_FILE.getDefaultValue());
    }

    @FXML void onQuarterChoice(ActionEvent ev) {
        operationDays.setText("90");
        operationDays.setDisable(true);
    }

    @FXML void onMonthChoice(ActionEvent ev) {
        operationDays.setText("30");
        operationDays.setDisable(true);
    }

    @FXML void onWeekChoice(ActionEvent ev) {
        operationDays.setText("7");
        operationDays.setDisable(true);
    }

    @FXML void onCustomChoice(ActionEvent ev) {
        operationDays.setDisable(false);
    }

    //region Private

    /**
     * Read settings from props and set control's values
     * Supposed to run in AppUI thread
     *
     * @param props - settings values
     * */
    private void displaySettings(Properties props) {
        int operDays;
        String sday = (String)props.getOrDefault(
                SettingType.OPERATION_DAYS.name(),
                SettingType.OPERATION_DAYS.getDefaultValue()
        );
        switch (sday) {
            case "90":
                onQuarterChoice(null);
                quarterPeriod.setSelected(true);
                operDays = 90;
                break;
            case "30":
                onMonthChoice(null);
                monthPeriod.setSelected(true);
                operDays = 30;
                break;
            case "7":
                onWeekChoice(null);
                weekPeriod.setSelected(true);
                operDays = 7;
                break;
            default:
                onCustomChoice(null);
                customPeriod.setSelected(true);
                operDays = parseIntOrDefault(sday, SettingType.OPERATION_DAYS.getDefaultValue());
        }
        operationDays.setText( String.valueOf(operDays) );
        refreshPeriod.setText(
                (String)props.getOrDefault(
                        SettingType.REFRESH_PERIOD.name(),
                        SettingType.REFRESH_PERIOD.getDefaultValue()
                )
        );
    }

    /**
     * Read setting's values from controls and return properties
     *
     * @return Properties read from controls
     * */
/*
    private Properties readControls() {
        Properties props = new Properties();
        int operDays = parseIntOrDefault(operationDays.getText(), SettingType.OPERATION_DAYS.getDefaultValue());
        props.setProperty(SettingType.OPERATION_DAYS.name(), String.valueOf(operDays));
        int refreshPrd = parseIntOrDefault(refreshPeriod.getText(), SettingType.REFRESH_PERIOD.getDefaultValue());
        props.setProperty(SettingType.REFRESH_PERIOD.name(), String.valueOf(refreshPrd));
        return props;
    }
*/

    /**
     * Parse text value to int. Default value will be used if parse fails
     *
     * @param text - value will be parsed to Int
     * @param defaultValue - value will be parsed if the text fails to parse
     * @return parsed value
     * */
    private int parseIntOrDefault(String text, String defaultValue) {
        int val = Integer.parseInt(defaultValue);
        try{
            val = Integer.parseInt(text);
        } catch (NumberFormatException ex){
            log.warning("unable to convert to int, value: " + text);
        }
        return val;
    }

    //endregion
}
