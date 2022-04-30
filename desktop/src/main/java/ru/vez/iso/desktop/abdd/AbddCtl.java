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
import ru.vez.iso.desktop.model.UserDetails;
import ru.vez.iso.desktop.shared.AppSettings;
import ru.vez.iso.desktop.shared.AppStateData;
import ru.vez.iso.desktop.shared.AppStateType;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

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
    private List<StorageUnitStatus> exStatusesFilter;
    private int period = 1;

    //endregion

    public AbddCtl(ObservableMap<AppStateType, AppStateData> appState, AbddSrv service) {
        this.service = service;
        this.appState = appState;

        // Add login listener
        this.appState.addListener(
                (MapChangeListener<AppStateType, AppStateData>) change -> {
                    if (AppStateType.USER_DETAILS.equals(change.getKey())) {
                        UserDetails userDetails = (UserDetails) change.getValueAdded().getValue();
                        Platform.runLater(()-> unlockControls(userDetails != UserDetails.NOT_SIGNED_USER));
                    }
                });

        // Data listener
        this.appState.addListener(
                (MapChangeListener<AppStateType, AppStateData>) change -> {
                    if (AppStateType.OPERATION_DAYS.equals(change.getKey())) {
                        List<OperatingDayFX> data = (List<OperatingDayFX>) change.getValueAdded().getValue();
                        Platform.runLater(()-> displayOperatingDays(data));
                    }
                });
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

        // add listener to select OpDays table should refresh slave table
        tblOperatingDays.getSelectionModel().selectedItemProperty().addListener(
                (o, old, newValue) -> {
                    if (newValue != null) {
                        this.filterAndDisplayStorageUnits(newValue.getStorageUnits(), exStatusesFilter);
                    }
                });

        // disable the "Write" button if no record selected
        butIsoLoad.disableProperty().bind(
                tblStorageUnits.getSelectionModel().selectedItemProperty().isNull()
        );

        // load default filter: operation's days period
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

        // listen for StoreUnitsStatus filter change (RadioButtons)
        filterGroup.selectedToggleProperty().addListener((o, old, newVal) -> {
            logger.debug("storageUnits filter: " + ((RadioButton) newVal).getText());
            if (exShowAvail == newVal) {
                exStatusesFilter = Collections.unmodifiableList(Arrays.asList(StorageUnitStatus.READY_TO_RECORDING, StorageUnitStatus.RECORDED));
            } else if (exShowPrep == newVal) {
                exStatusesFilter = Collections.singletonList(StorageUnitStatus.PREPARING_RECORDING);
            } else {
                exStatusesFilter = null;
            }
            // re-filter storageUnits and display
            filterAndDisplayStorageUnits(storageUnits, exStatusesFilter);
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
        service.loadISOAsync(selected.getObjectId());
    }

    //region Private

    private void unlockControls(boolean unlock) {
        butReload.setDisable(!unlock);
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
