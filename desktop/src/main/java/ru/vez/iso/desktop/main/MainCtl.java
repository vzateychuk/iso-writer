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
    @FXML private TableColumn<OperatingDayFX, String> edited;

    // Таблица "Список единиц хранения"
    @FXML private TableView<StorageUnitFX> tblStorageUnits;
    @FXML private TableColumn<StorageUnitFX, String> numberSu;
    @FXML private TableColumn<StorageUnitFX, String> creationDate;
    @FXML private TableColumn<StorageUnitFX, Integer> dataSize;
    @FXML private TableColumn<StorageUnitFX, String> storageDate;
    @FXML private TableColumn<StorageUnitFX, String> storageUnitStatus;
    @FXML private TableColumn<StorageUnitFX, String> savingDate;
    @FXML private TableColumn<StorageUnitFX, String> fileName;

    // Кнопки
    @FXML private Button butIsoLoad;
    @FXML private Button butReload;
    @FXML private Button butBurn;
    @FXML private Button butDelete;
    @FXML private Button butCheckSum;
    @FXML public Button butIsoCreate;

    // RadioButtons - filter StoreUnits
    @FXML private ToggleGroup filterGroup;
    @FXML private RadioButton exShowAll;
    @FXML private RadioButton exShowAvail;
    @FXML private RadioButton exShowPrep;

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final MainSrv mainSrv;
    private final CacheSrv cacheSrv;

    private ObservableList<OperatingDayFX> operatingDays;
    private ObservableList<StorageUnitFX> storageUnits;
    private List<StorageUnitStatus> statusesFilter;
    private int period = 1;

    //endregion

    public MainCtl(ObservableMap<AppStateType, AppStateData> appState, MainSrv mainSrv, CacheSrv cacheSrv) {
        this.appState = appState;
        this.mainSrv = mainSrv;
        this.cacheSrv = cacheSrv;
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
        edited.setCellValueFactory(cell -> cell.getValue().editedProperty());

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
            butIsoLoad.setDisable(newVal == null);
            butIsoCreate.setDisable(newVal == null);
            butBurn.setDisable(newVal == null);
            butDelete.setDisable(newVal == null);
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
                    }
                });

        // StoreUnitsStatus status filter (RadioButtons)
        filterGroup.selectedToggleProperty().addListener((o, old, newVal) -> {
            if (exShowAvail == newVal) {
                statusesFilter = Collections.unmodifiableList(Arrays.asList(StorageUnitStatus.READY_TO_RECORDING, StorageUnitStatus.RECORDED));
            } else if (exShowPrep == newVal) {
                statusesFilter = Collections.singletonList(StorageUnitStatus.PREPARING_RECORDING);
            } else {
                statusesFilter = null;
            }
            // re-filter storageUnits and display
            this.filterAndDisplayStorageUnits(storageUnits, statusesFilter);
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
    }

    /**
     * Reload starts OperationDay's and StorageUnit's refresh
     * */
    @FXML public void onReload(ActionEvent ev) {
        logger.debug("");
        int days = period++;
        try {
            days = Integer.parseUnsignedInt(operationDays.getText());
        } catch ( NumberFormatException ex) {
            logger.warn("can't parse value to int: " + operationDays.getText());
        }
        mainSrv.readOpsDayAsync(days);
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
        cacheSrv.loadISOAsync(selected.getNumberSu());
    }

    /**
     * Burn ISO disk
     * */
    @FXML public void onBurnIso(ActionEvent ev) {
        logger.debug("Запись диска");
    }

    /**
     * Delete ISO files from cache
     * */
    @FXML public void onDeleteIso(ActionEvent ev) {
        StorageUnitFX su = tblStorageUnits.getSelectionModel().selectedItemProperty().getValue();
        logger.debug(su.getIsoFileName());
        if (Strings.isBlank(su.getIsoFileName())) {
            logger.warn("requested for empty file");
            appState.put(AppStateType.NOTIFICATION, AppStateData.builder().value("Файл не выбран : " + fileName).build());
            return;
        }
        cacheSrv.deleteFileAndReload(su.getIsoFileName());
    }

    /**
     * Checking the control sum of ISO files
     * */
    @FXML public void onCheckSum(ActionEvent ev) {

        String currentPath = (String)appState.get(AppStateType.ZIP_DIR).getValue();
        if (Strings.isBlank(currentPath)) {
            appState.put(AppStateType.NOTIFICATION, AppStateData.builder().value("Невозможно выполнить проверку контрольной суммы. Не открыт DIR.zip").build());
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
                                    fileName
                            );
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
        exShowAvail.setDisable(filtered.size() == 0);
        exShowPrep.setDisable(filtered.size() == 0);
        this.tblStorageUnits.setItems(filtered);
    }

    //endregion
}
