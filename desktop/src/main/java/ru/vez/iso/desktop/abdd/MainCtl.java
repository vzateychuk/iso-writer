package ru.vez.iso.desktop.abdd;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import lombok.extern.java.Log;
import ru.vez.iso.desktop.model.UserDetails;
import ru.vez.iso.desktop.state.AppStateData;
import ru.vez.iso.desktop.state.AppStateType;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for: "Выбор Единицы хранения для записи на диск"
 * */
@Log
public class MainCtl implements Initializable {

    // Таблица "Список операционных дней"
    @FXML private TableView<OperatingDayFX> tblOperatingDays;
    @FXML private TableColumn<OperatingDayFX, String> operatingDay;
    @FXML private TableColumn<OperatingDayFX, TypeSu> typeSu;
    @FXML private TableColumn<OperatingDayFX, String> status;
    @FXML private TableColumn<OperatingDayFX, String> createdAt;
    @FXML private TableColumn<OperatingDayFX, Boolean> edited;

    // Таблица "Список единиц хранения"
    @FXML private TableView<StorageUnitFX> tblStorageUnits;
    @FXML private TableColumn<StorageUnitFX, String> numberSu;
    @FXML private TableColumn<StorageUnitFX, String> creationDate;
    @FXML private TableColumn<StorageUnitFX, Integer> dataSize;
    @FXML private TableColumn<StorageUnitFX, String> storageDate;
    @FXML private TableColumn<StorageUnitFX, String> storageUnitStatus;
    @FXML private TableColumn<StorageUnitFX, String> savingDate;

    // Кнопки
    @FXML private Button butWrite;
    @FXML private Button butReload;

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final MainSrv service;

    private ObservableList<OperatingDayFX> operatingDays;
    private ObservableList<StorageUnitFX> storageUnits;
    private int period = 1;

    public MainCtl(ObservableMap<AppStateType, AppStateData> appState, MainSrv service) {
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
        // Setting UI
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

        // add listener to select master table should refresh slave table
        tblOperatingDays.getSelectionModel().selectedItemProperty()
            .addListener(
                (o, old, newValue) -> {
                    if (newValue != null) {
                        this.displayStorageUnits(newValue.getStorageUnits());
                    }
                });

        // disable the "Write" button if none selected
        butWrite.disableProperty().bind(
                tblStorageUnits.getSelectionModel().selectedItemProperty().isNull()
        );

        this.onReload(null);
    }


    @FXML public void onReload(ActionEvent ev) {
        service.loadOpsDayAsync(period++);
    }

    @FXML void onWrite(ActionEvent ev) {
        log.info("MainCtl.onWrite");
    }

    //region Private

    private void unlockControls(boolean unlock) {
        butReload.setDisable(!unlock);
    }

    /**
     * Refresh master OperatingDays table
     */
    private void displayOperatingDays(List<OperatingDayFX> operatingDays) {
        this.operatingDays = FXCollections.observableList(operatingDays);
        tblOperatingDays.setItems(this.operatingDays);
    }

    /**
     * Refresh slave storageUnits table
     */
    private void displayStorageUnits(List<StorageUnitFX> storageUnits) {
        this.storageUnits = FXCollections.observableList(storageUnits);
        tblStorageUnits.setItems(this.storageUnits);
    }

    //endregion
}
