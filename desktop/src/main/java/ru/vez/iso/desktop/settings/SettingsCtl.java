package ru.vez.iso.desktop.settings;

import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
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
    //
    @FXML private TextField operationDays;
    @FXML private TextField refreshPeriod;
    @FXML private TextField abddAPI;
    @FXML private TextField fileCache;

    // radio-buttons group
    @FXML public Button radioQuarter;
    @FXML public Button radioQuarterInner;
    @FXML public Button radioMonth;
    @FXML public Button radioMonthInner;
    @FXML public Button radioWeek;
    @FXML public Button radioWeekInner;
    @FXML public Button radioCustom;
    @FXML public Button radioCustomInner;
    private final RadioButtonsToggle radioButtonsToggle;

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final SettingsSrv service;

    public SettingsCtl(ObservableMap<AppStateType, AppStateData> appState, SettingsSrv service) {
        this.appState = appState;
        this.service = service;

        this.radioButtonsToggle = new RadioButtonsToggle();
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug(location);

        this.appState.addListener(
                (MapChangeListener<AppStateType, AppStateData>) change -> {
                    if (AppStateType.SETTINGS.equals(change.getKey())) {
                        AppSettings sets = ((AppStateData<AppSettings>)appState.get(AppStateType.SETTINGS)).getValue();
                        Platform.runLater( ()->displaySettings(sets) );
                    }
                });

        // RadioButtons
        this.radioButtonsToggle.add(radioQuarter, radioQuarterInner);
        this.radioButtonsToggle.add(radioMonth, radioMonthInner);
        this.radioButtonsToggle.add(radioWeek, radioWeekInner);
        this.radioButtonsToggle.add(radioCustom, radioCustomInner);
    }

    @FXML public void onSave(ActionEvent ev) {
        logger.debug("");

        AppSettings currentSettings = ((AppStateData<AppSettings>)appState.get(AppStateType.SETTINGS)).getValue();

        int operDays = UtilsHelper.parseIntOrDefault(operationDays.getText(), SettingType.OPERATION_DAYS.getDefaultValue());
        int refreshMin = UtilsHelper.parseIntOrDefault(refreshPeriod.getText(), SettingType.REFRESH_PERIOD.getDefaultValue());

        AppSettings newSetting = AppSettings.builder()
                .backendAPI(abddAPI.getText())
                .filterOpsDays(operDays)
                .settingFile(currentSettings.getSettingFile())
                .isoCachePath(fileCache.getText())
                .refreshMin(refreshMin)
                .build();

        service.saveAsync(newSetting);
    }

    @FXML void onQuarterChoice(ActionEvent ev) {
        radioButtonsToggle.setActive(radioQuarter);
        operationDays.setText("90");
        operationDays.setDisable(true);
    }

    @FXML void onMonthChoice(ActionEvent ev) {
        radioButtonsToggle.setActive(radioMonth);
        operationDays.setText("30");
        operationDays.setDisable(true);
    }

    @FXML void onWeekChoice(ActionEvent ev) {
        radioButtonsToggle.setActive(radioWeek);
        operationDays.setText("7");
        operationDays.setDisable(true);
    }

    @FXML void onCustomChoice(ActionEvent ev) {
        radioButtonsToggle.setActive(radioCustom);
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
                radioButtonsToggle.setActive(radioQuarter);
                break;
            case 30:
                onMonthChoice(null);
                radioButtonsToggle.setActive(radioMonth);
                break;
            case 7:
                onWeekChoice(null);
                radioButtonsToggle.setActive(radioWeek);
                break;
            default:
                onCustomChoice(null);
                radioButtonsToggle.setActive(radioCustom);
        }
        operationDays.setText( String.valueOf(operDays) );
        refreshPeriod.setText( String.valueOf(sets.getRefreshMin()) );
        fileCache.setText( sets.getIsoCachePath() );
        abddAPI.setText( sets.getBackendAPI() );
    }

    //endregion
}
