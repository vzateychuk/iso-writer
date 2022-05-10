package ru.vez.iso.desktop.settings;

import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.shared.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for View "Форма настроек"
 * */
public class SettingsCtl implements Initializable {

    private static final Logger logger = LogManager.getLogger();

    @FXML private ToggleGroup opsPeriodGroup;
    @FXML private RadioButton quarterPeriod;
    @FXML private RadioButton monthPeriod;
    @FXML private RadioButton weekPeriod;
    @FXML private RadioButton customPeriod;
    @FXML private TextField operationDays;
    @FXML private TextField refreshPeriod;
    @FXML private TextField abddAPI;
    @FXML private TextField fileCache;

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final SettingsSrv service;

    public SettingsCtl(ObservableMap<AppStateType, AppStateData> appState, SettingsSrv service) {
        this.appState = appState;
        this.service = service;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug(location);

        this.appState.addListener(
                (MapChangeListener<AppStateType, AppStateData>) change -> {
                    if (AppStateType.SETTINGS.equals(change.getKey())) {
                        AppSettings sets = ((AppStateData<AppSettings>)appState.get(AppStateType.SETTINGS)).getValue();
                        Platform.runLater(()->displaySettings(sets));
                    }
                });
    }

    @FXML public void onSave(ActionEvent ev) {
        logger.debug("");

        AppSettings curSettings = ((AppStateData<AppSettings>)appState.get(AppStateType.SETTINGS)).getValue();

        int operDays = UtilsHelper.parseIntOrDefault(operationDays.getText(), SettingType.OPERATION_DAYS.getDefaultValue());
        int refreshMin = UtilsHelper.parseIntOrDefault(refreshPeriod.getText(), SettingType.REFRESH_PERIOD.getDefaultValue());

        AppSettings newSetting = AppSettings.builder()
                .abddAPI(abddAPI.getText())
                .filterOpsDays(operDays)
                .settingFile(curSettings.getSettingFile())
                .isoCachePath(fileCache.getText())
                .refreshMin(refreshMin)
                .build();

        service.saveAsync(newSetting);
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

    //region PRIVATE

    /**
     * Read settings from props and set control's values
     * Supposed to run in AppUI thread
     *  @param sets - settings values
     * */
    private void displaySettings(AppSettings sets) {
        int operDays = sets.getFilterOpsDays();
        switch (operDays) {
            case 90:
                onQuarterChoice(null);
                quarterPeriod.setSelected(true);
                break;
            case 30:
                onMonthChoice(null);
                monthPeriod.setSelected(true);
                break;
            case 7:
                onWeekChoice(null);
                weekPeriod.setSelected(true);
                break;
            default:
                onCustomChoice(null);
                customPeriod.setSelected(true);
        }
        operationDays.setText( String.valueOf(operDays) );
        refreshPeriod.setText( String.valueOf(sets.getRefreshMin()) );
        fileCache.setText( sets.getIsoCachePath() );
        abddAPI.setText( sets.getAbddAPI() );
    }

    //endregion
}
