package ru.vez.iso.desktop.main;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.extern.java.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import ru.vez.iso.desktop.main.filecache.FileCacheSrv;
import ru.vez.iso.desktop.main.operdays.OperatingDayFX;
import ru.vez.iso.desktop.main.operdays.TypeSu;
import ru.vez.iso.desktop.main.storeunits.StorageUnitFX;
import ru.vez.iso.desktop.main.storeunits.StorageUnitStatus;
import ru.vez.iso.desktop.shared.*;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for: "Выбор Единицы хранения для записи на диск"
 * */
@Log
public class MainCtl implements Initializable {

    //region Properties
    private static final Logger logger = LogManager.getLogger();

    // Фильтр "Список операционных дней"
    @FXML private TextField operationDays;

    // Таблица "Список операционных дней"
    @FXML private TableView<OperatingDayFX> tblOperatingDays;
    @FXML private TableColumn<OperatingDayFX, String> operatingDay;
    @FXML private TableColumn<OperatingDayFX, TypeSu> typeSu;
    @FXML private TableColumn<OperatingDayFX, String> status;
    @FXML private TableColumn<OperatingDayFX, String> createdAt;

    // Таблица "Список единиц хранения"
    @FXML private TableView<StorageUnitFX> tblStorageUnits;
    @FXML private TableColumn<StorageUnitFX, String> numberSu;
    @FXML private TableColumn<StorageUnitFX, String> creationDate;
    @FXML private TableColumn<StorageUnitFX, Long> dataSize;
    @FXML private TableColumn<StorageUnitFX, String> storageDate;
    @FXML private TableColumn<StorageUnitFX, String> storageUnitStatus;
    @FXML private TableColumn<StorageUnitFX, String> savingDate;
    @FXML private TableColumn<StorageUnitFX, String> fileName;
    @FXML public TableColumn <StorageUnitFX, String> deleted;

    // Кнопки
    @FXML private Button butIsoLoad;
    @FXML private Button butReload;
    @FXML private Button butBurn;
    @FXML private Button butDelete;
    @FXML private Button butCheckSum;
    @FXML public Button butIsoCreate;

    // RadioButtons - filter StoreUnits
    @FXML public Button radioStatusAll;
    @FXML public Button radioStatusAllInner;
    @FXML public Button radioStatusAvailable;
    @FXML public Button radioStatusAvailableInner;
    @FXML public Button radioStatusPrepared;
    @FXML public Button radioStatusPreparedInner;
    private final RadioButtonsToggle radioButtonsToggle;

    // Internal state
    private final ObservableMap<AppStateType, AppStateData> appState;
    private final MainSrv mainSrv;
    private final FileCacheSrv fileCacheSrv;
    private final MessageSrv msgSrv;

    private ObservableList<OperatingDayFX> operatingDays;
    private ObservableList<StorageUnitFX> storageUnits;
    private List<StorageUnitStatus> statusesFilter;

    //endregion

    public MainCtl(ObservableMap<AppStateType,
                   AppStateData> appState,
                   MainSrv mainSrv,
                   FileCacheSrv fileCacheSrv,
                   MessageSrv msgSrv) {
        this.appState = appState;
        this.mainSrv = mainSrv;
        this.fileCacheSrv = fileCacheSrv;
        this.msgSrv = msgSrv;
        this.radioButtonsToggle = new RadioButtonsToggle();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug(location);

        // Setting table Operation Days
        operatingDays = FXCollections.emptyObservableList();
        tblOperatingDays.setItems(operatingDays);
        tblOperatingDays.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // set selection mode to only 1 row
        tblOperatingDays.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Таблица "Список операционных дней"
        operatingDay.setCellValueFactory(cell -> cell.getValue().operatingDayProperty());
        typeSu.setCellValueFactory(cell -> cell.getValue().typeSuProperty());
        status.setCellValueFactory(cell -> cell.getValue().statusProperty());
        createdAt.setCellValueFactory(cell -> cell.getValue().createdAtProperty());

        // Таблица "Список единиц хранения"
        storageUnits = FXCollections.emptyObservableList();
        tblStorageUnits.setItems(storageUnits);
        tblStorageUnits.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // set selection mode to only 1 row
        tblStorageUnits.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        numberSu.setCellValueFactory(cell -> cell.getValue().numberSuProperty());
        creationDate.setCellValueFactory(cell -> cell.getValue().creationDateProperty());
        dataSize.setCellValueFactory(cell -> cell.getValue().dataSizeProperty());
        storageDate.setCellValueFactory(cell -> cell.getValue().storageDateProperty());
        storageUnitStatus.setCellValueFactory(cell -> cell.getValue().storageUnitStatusProperty());
        savingDate.setCellValueFactory(cell -> cell.getValue().savingDateProperty());
        fileName.setCellValueFactory(cell -> cell.getValue().isoFileNameProperty());
        deleted.setCellValueFactory(cell -> cell.getValue().deletedProperty());

        // Operation Days table listener
        this.appState.addListener(
                (MapChangeListener<AppStateType, AppStateData>) change -> {
                    if (AppStateType.OPERATION_DAYS.equals(change.getKey())) {
                        List<OperatingDayFX> data = (List<OperatingDayFX>) change.getValueAdded().getValue();
                        Platform.runLater(()-> displayOperatingDays(data));
                    }
                });

        // OperationDays: when select row should refresh StorageUnits table
        tblOperatingDays.getSelectionModel().selectedItemProperty().addListener(
                (o, old, newValue) -> {
                    if (newValue != null) {
                        // need to update storage units with isoFileName, stored in local file-cache
                        final List<IsoFileFX> fileCache = ((AppStateData<List<IsoFileFX>>)appState.get(AppStateType.ISO_FILES_NAMES)).getValue();
                        List<StorageUnitFX> withFileNames = this.getWithFileName(newValue.getStorageUnits(), fileCache);
                        // filter and display a storage Units
                        Platform.runLater(()-> this.filterAndDisplayStorageUnits(withFileNames, statusesFilter));
                    }
                });

        // disable buttons if no record selected
        tblStorageUnits.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal) -> {
            butIsoLoad.setDisable(newVal == null || newVal.isDeleted());
            butIsoCreate.setDisable(newVal == null || !newVal.isDeleted());
            butBurn.setDisable(newVal == null || Strings.isBlank(newVal.getIsoFileName())
                    || !Collections.unmodifiableList(Arrays.asList(StorageUnitStatus.READY_TO_RECORDING, StorageUnitStatus.RECORDED)).contains(newVal.getStorageUnitStatus()));
            butDelete.setDisable(newVal == null || Strings.isBlank(newVal.getIsoFileName()));
            butCheckSum.setDisable(newVal == null);
        });

        // Operation Days period's filter (when settings changes)
        this.appState.addListener(
                (MapChangeListener<AppStateType, AppStateData>) change -> {
                    if (AppStateType.SETTINGS.equals(change.getKey())) {
                        AppSettings sets = ((AppStateData<AppSettings>)appState.get(AppStateType.SETTINGS)).getValue();
                        int filterDays = sets.getFilterOpsDays();
                        Platform.runLater( ()-> {
                            operationDays.setText(String.valueOf(filterDays));
                            this.onReload(null);
                        } );

                        // reschedule "reload" with the new param time
                        int refreshPeriod = sets.getRefreshMin();
                        mainSrv.scheduleReadInterval(refreshPeriod, filterDays);
                    }
                });


        // ISO_FILES in cache changed
        this.appState.addListener(
                (MapChangeListener<AppStateType, AppStateData>) change -> {
                    if (AppStateType.ISO_FILES_NAMES.equals(change.getKey())) {
                        List<IsoFileFX> fileCache = (List<IsoFileFX>) change.getValueAdded().getValue();
                        List<StorageUnitFX> withFileNames = this.getWithFileName(this.storageUnits, fileCache);
                        // filter and display a storage Units with fileNames
                        Platform.runLater( ()-> this.filterAndDisplayStorageUnits(withFileNames, statusesFilter) );
                    }
                });

        // StoreUnitsStatus status filter (RadioButtons)
        this.radioButtonsToggle.add(radioStatusAll, radioStatusAllInner);
        this.radioButtonsToggle.add(radioStatusAvailable, radioStatusAvailableInner);
        this.radioButtonsToggle.add(radioStatusPrepared, radioStatusPreparedInner);
        this.radioButtonsToggle.setActive(radioStatusAll);
    }

    /**
     * Choice filterStatusAll - Все
     * */
    @FXML public void onStatusAllChoice(ActionEvent ev) {
        logger.debug("");
        this.radioButtonsToggle.setActive(radioStatusAll);
        this.statusesFilter = null;
        this.filterAndDisplayStorageUnits(storageUnits, null);
    }

    /**
     * Choice filterStatusAvailable - Доступные для записи
     * */
    @FXML public void onStatusAvailableChoice(ActionEvent ev) {
        logger.debug("");
        this.radioButtonsToggle.setActive(radioStatusAvailable);
        this.statusesFilter = Collections.unmodifiableList(Arrays.asList(StorageUnitStatus.READY_TO_RECORDING, StorageUnitStatus.RECORDED));
        this.filterAndDisplayStorageUnits(storageUnits, this.statusesFilter);
    }

    /**
     * Choice filterStatusPrepared - Готовые для записи
     * */
    @FXML public void onStatusShowPrepChoice(ActionEvent ev) {
        logger.debug("");
        this.radioButtonsToggle.setActive(radioStatusPrepared);
        this.statusesFilter = Collections.singletonList(StorageUnitStatus.PREPARATION_FOR_RECORDING);
        this.filterAndDisplayStorageUnits(storageUnits, this.statusesFilter);
    }

    /**
     * Reload starts OperationDay's and StorageUnit's refresh
     * */
    @FXML public void onReload(ActionEvent ev) {
        logger.debug("");
        int days = 1;
        try {
            days = Integer.parseUnsignedInt(operationDays.getText());
        } catch ( NumberFormatException ex) {
            logger.warn("can't parse value to int: " + operationDays.getText());
        }
        mainSrv.readDataAsync(days);
    }

    /**
     * Fire Reload when user ENTER key on Filter button
     * */
    @FXML public void onFilterEnter(KeyEvent ke) {
        if( ke.getCode() == KeyCode.ENTER ) {
            onReload(null);
        }
    }

    /**
     * Request backend (ABDD system) to create the ISO
     * */
    @FXML public void onIsoCreate(ActionEvent ev) {
        StorageUnitFX selected = tblStorageUnits.getSelectionModel().getSelectedItem();
        logger.debug("{}", selected.getNumberSu());
        mainSrv.isoCreateAsync(selected);
    }

    /**
     * Load ISO file from ABDD server
     * */
    @FXML void onStartIsoLoad(ActionEvent ev) {
        StorageUnitFX selected = tblStorageUnits.getSelectionModel().getSelectedItem();
        logger.debug("{}", selected.getNumberSu());
        fileCacheSrv.loadISOAsync(selected.getNumberSu());
    }

    /**
     * Burn ISO disk
     * */
    @FXML public void onBurnIso(ActionEvent ev) {

        String diskLabel = UtilsHelper.getDiskLabel();
        if (Strings.isBlank(diskLabel)) {
            return;
        }
        logger.debug(diskLabel);
    }

    /**
     * Delete ISO files from cache
     * */
    @FXML public void onDeleteIso(ActionEvent ev) {
        StorageUnitFX su = tblStorageUnits.getSelectionModel().selectedItemProperty().getValue();
        logger.debug(su.getIsoFileName());
        if (Strings.isBlank(su.getIsoFileName())) {
            this.msgSrv.news("Файл не выбран : " + fileName);
            return;
        }
        fileCacheSrv.deleteFileAndReload(su.getIsoFileName());
    }

    /**
     * Checking the control sum of ISO files
     * */
    @FXML public void onCheckSum(ActionEvent ev) {

        String currentPath = (String)appState.get(AppStateType.ZIP_DIR).getValue();
        if (Strings.isBlank(currentPath)) {
            this.msgSrv.news("Невозможно выполнить проверку контрольной суммы. DIR.zip не открыт");
            logger.warn("DIR.zip path not defined, exit");
            return;
        }
        logger.debug(currentPath);

        Path dirZip = Paths.get(currentPath, MyConst.DIR_ZIP);
        mainSrv.checkSumAsync(dirZip);
    }

    //region PRIVATE

    /**
     * Update storeUnit fileName property if there is a filename in fileCache found
     * */
    List<StorageUnitFX> getWithFileName(List<StorageUnitFX> storageUnits, List<IsoFileFX> fileCache) {

        return storageUnits.stream()
                .map(su -> {
                    StorageUnitFX updated = su;
                    if ( !Strings.isBlank(su.getNumberSu()) ) {
                        String fullName = su.getNumberSu() + ".iso";
                        final String fileName = fileCache.stream().anyMatch(f -> f.getFileName().equals(fullName)) ? fullName : "";
                        updated = new StorageUnitFX(
                                    su.getObjectId(),
                                    su.getOperatingDayId(),
                                    su.getNumberSu(),
                                    su.getCreationDate(),
                                    su.getDataSize(),
                                    su.getStorageDate(),
                                    su.getStorageUnitStatus(),
                                    su.getSavingDate(),
                                    fileName,
                                    su.isDeleted());
                    };
                    return updated;
                })
                .collect(Collectors.toList());
    }

    /**
     * Refresh master OperatingDays table
     * Must be executed in Main Application Thread only!
     */
    private void displayOperatingDays(List<OperatingDayFX> operatingDays) {
        this.operatingDays = FXCollections.observableList(operatingDays);
        tblOperatingDays.setItems(this.operatingDays);
    }

    /**
     * Refresh slave storageUnits table
     * Must be executed in Main Application Thread only!
     */
    private void filterAndDisplayStorageUnits(List<StorageUnitFX> storageUnits, List<StorageUnitStatus> filter) {

        logger.debug("filter: {}", filter);
        this.storageUnits = FXCollections.observableList(storageUnits);
        // filter storageUnits if filter is not null
        ObservableList<StorageUnitFX> filtered =
                filter == null
                        ? this.storageUnits
                        : this.storageUnits.filtered(su -> filter.stream().anyMatch(f -> f.equals(su.getStorageUnitStatus())));
        // disable the storageUnits filter (radio-buttons) if no data available
        this.tblStorageUnits.setItems(filtered);
    }

    //endregion
}
