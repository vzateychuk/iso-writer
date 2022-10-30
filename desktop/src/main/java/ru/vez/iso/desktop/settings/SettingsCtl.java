package ru.vez.iso.desktop.settings;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import ru.vez.iso.desktop.burn.BurnSrv;
import ru.vez.iso.desktop.burn.RecorderInfo;
import ru.vez.iso.desktop.shared.AppSettings;
import ru.vez.iso.desktop.shared.RadioButtonsToggle;
import ru.vez.iso.desktop.shared.SettingType;
import ru.vez.iso.desktop.shared.UtilsHelper;
import ru.vez.iso.desktop.state.ApplicationState;
import ru.vez.iso.desktop.state.RunMode;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for View "Форма настроек"
 * */
public class SettingsCtl implements Initializable {

    private static final Logger logger = LogManager.getLogger();

    // Settings controls
    @FXML public Button driveInfo;
    @FXML private TextField operationDays;
    @FXML private TextField refreshPeriod;
    @FXML private TextField backendAPI;
    @FXML public TextField authAPI;
    @FXML public TextField authPath;
    @FXML private TextField fileCache;
    @FXML private TextField evictCacheDays; // Days, period of cache eviction

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

    private final ApplicationState state;
    private final SettingsSrv service;
    private final BurnSrv burnSrv;

    public SettingsCtl(ApplicationState appState, SettingsSrv service, BurnSrv burnSrv) {
        this.state = appState;
        this.service = service;
        this.burnSrv = burnSrv;
        this.radioButtonsToggle = new RadioButtonsToggle();
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        logger.debug(location);

        this.state.settingsProperty().addListener(
                (ob,oldVal,newVal) -> Platform.runLater( ()->displaySettings(newVal) )
        );

        // RadioButtons
        this.radioButtonsToggle.add(radioQuarter, radioQuarterInner);
        this.radioButtonsToggle.add(radioMonth, radioMonthInner);
        this.radioButtonsToggle.add(radioWeek, radioWeekInner);
        this.radioButtonsToggle.add(radioCustom, radioCustomInner);

        // force the field to be numeric only
        this.operationDays.textProperty().addListener((o, old, newValue) -> {
            if (!newValue.matches("\\d*")) {
                operationDays.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // force the field to be numeric only
        this.refreshPeriod.textProperty().addListener((o, old, newValue) -> {
            if (!newValue.matches("\\d*")) {
                refreshPeriod.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // force the field to be numeric only
        this.evictCacheDays.textProperty().addListener((o, old, newValue) -> {
            if (!newValue.matches("\\d*")) {
                evictCacheDays.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        this.driveInfo.setDisable( this.state.getRunMode() == RunMode.PROD );
        this.driveInfo.setVisible( this.state.getRunMode() != RunMode.PROD );
    }

    @FXML public void onSave(ActionEvent ev) {

        AppSettings currentSettings = state.getSettings();

        // Validation for values
        if (Strings.isBlank(operationDays.getText())) {
            logger.warn("Empty operationDays: {}", operationDays.getText());
            UtilsHelper.getConfirmation("Необходимо указать период отображения операционных дней.");
            return;
        }
        int operDays = UtilsHelper.parseIntOrDefault(operationDays.getText(), SettingType.OPERATION_DAYS.getDefaultValue());

        if (Strings.isBlank(refreshPeriod.getText())) {
            logger.warn("Empty refreshPeriod: {}", refreshPeriod.getText());
            UtilsHelper.getConfirmation("Необходимо указать период обновления списка операционных дней.");
            return;
        }
        int refreshMin = UtilsHelper.parseIntOrDefault(refreshPeriod.getText(), SettingType.REFRESH_PERIOD.getDefaultValue());

        if (Strings.isBlank(evictCacheDays.getText())) {
            logger.warn("Empty evictCacheDays: {}", evictCacheDays.getText());
            UtilsHelper.getConfirmation("Необходимо указать срок хранения iso-образов, дней.");
            return;
        }
        int evictDays = UtilsHelper.parseIntOrDefault(evictCacheDays.getText(), SettingType.EVICT_CACHE_DAYS.getDefaultValue());

        if (Strings.isBlank(backendAPI.getText())) {
            logger.warn("Empty backendAPI: {}", backendAPI.getText());
            UtilsHelper.getConfirmation("Необходимо указать адрес сервера данных.");
            return;
        }

        if (Strings.isBlank(authAPI.getText())) {
            logger.warn("Empty authAPI: {}", authAPI.getText());
            UtilsHelper.getConfirmation("Необходимо указать адрес сервера авторизации.");
            return;
        }

        if (Strings.isBlank(authPath.getText())) {
            logger.warn("Empty authPath: {}", authPath.getText());
            UtilsHelper.getConfirmation("Необходимо указать путь для авторизации.");
            return;
        }

        AppSettings newSetting = AppSettings.builder()
                .filterOpsDays(operDays)
                .settingFile(currentSettings.getSettingFile())
                .isoCachePath(fileCache.getText())
                .refreshMin(refreshMin)
                .evictCacheDays(evictDays)
                .backendAPI(backendAPI.getText())
                .authAPI(authAPI.getText())
                .authPath(authPath.getText())
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

    @FXML  public void onDriveInfo(ActionEvent av) {
        RecorderInfo info = burnSrv.recorderInfo(0);
        logger.info(info);
        UtilsHelper.getConfirmation(info.toString());
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
        backendAPI.setText( sets.getBackendAPI() );
        authAPI.setText( sets.getAuthAPI() );
        authPath.setText( sets.getAuthPath() );
        evictCacheDays.setText( String.valueOf(sets.getEvictCacheDays()) );
    }

    //endregion
}
