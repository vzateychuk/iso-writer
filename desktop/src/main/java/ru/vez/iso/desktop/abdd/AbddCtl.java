package ru.vez.iso.desktop.abdd;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.extern.java.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import ru.vez.iso.desktop.shared.*;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for: "Выбор Единицы хранения для записи на диск"
 * */
@Log
public class AbddCtl implements Initializable {

    //region Properties
    private static Logger logger = LogManager.getLogger();

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

    // RadioButtons - filter StoreUnits
    @FXML private ToggleGroup filterGroup;
    @FXML private RadioButton exShowAll;
    @FXML private RadioButton exShowAvail;
    @FXML private RadioButton exShowPrep;

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final AbddSrv service;

    private ObservableList<OperatingDayFX> operatingDays;
    private ObservableList<StorageUnitFX> storageUnits;
    private List<StorageUnitStatus> statusesFilter;
    private int period = 1;

    //endregion

    public AbddCtl(ObservableMap<AppStateType, AppStateData> appState, AbddSrv service) {
        this.service = service;
        this.appState = appState;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("initialize");

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

        // disable the "Write" button if no record selected
        butIsoLoad.disableProperty().bind(
                tblStorageUnits.getSelectionModel().selectedItemProperty().isNull()
        );

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
            logger.debug("storageUnits filter: " + ((RadioButton) newVal).getText());
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

    @FXML public void onReload(ActionEvent ev) {
        int days = period++;
        try {
            days = Integer.parseUnsignedInt(operationDays.getText());
        } catch ( NumberFormatException ex) {
            logger.warn("can't parse value to int: " + operationDays.getText());
        }
        service.readOpsDayAsync(days);
    }

    @FXML void onStartIsoLoad(ActionEvent ev) {
        logger.debug("onStartIsoLoad");
        StorageUnitFX selected = tblStorageUnits.getSelectionModel().getSelectedItem();
        service.loadISOAsync(selected.getNumberSu());
    }

    //region PRIVATE

    /**
     * Update storeUnit fileName property if there is a filename in fileCache found
     * */
    private List<StorageUnitFX> getWithFileName(List<StorageUnitFX> storageUnits, List<IsoFileFX> fileCache) {

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
