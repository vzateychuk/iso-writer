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
import lombok.extern.java.Log;
import ru.vez.iso.desktop.model.ExStatus;
import ru.vez.iso.desktop.model.ExType;
import ru.vez.iso.desktop.model.OperatingDayFX;
import ru.vez.iso.desktop.model.UserDetails;
import ru.vez.iso.desktop.state.AppStateData;
import ru.vez.iso.desktop.state.AppStateType;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for: "Выбор Единицы хранения для записи на диск"
 * */
@Log
public class MainCtl implements Initializable {

    // Таблица "Список операционных дней"
    @FXML private TableView<OperatingDayFX> tblOperatingDays;
    @FXML private TableColumn<OperatingDayFX, LocalDate> operatingDay;
    @FXML private TableColumn<OperatingDayFX, ExType> exType;
    @FXML private TableColumn<OperatingDayFX, ExStatus> status;
    @FXML private TableColumn<OperatingDayFX, LocalDate> createdAt;
    @FXML private TableColumn<OperatingDayFX, Boolean> edited;

    // Таблица "Список единиц хранения"
/*
    @FXML private TableView<?> tblStorageUnits;
    @FXML private TableColumn<?, ?> numberEx;
    @FXML private TableColumn<?, ?> shelfLifeEx;
    @FXML private TableColumn<?, ?> statusEx;
    @FXML private TableColumn<?, ?> statusIsoEx;
    @FXML private TableColumn<?, ?> writeDateEx;
    @FXML private TableColumn<?, ?> createdEx;
    @FXML private TableColumn<?, ?> isoSizeEx;
*/

    // Кнопки
    @FXML private Button butWrite;
    @FXML private Button butReload;

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final MainSrv service;

    private ObservableList<OperatingDayFX> operatingDays;
    private int period = 1;

    public MainCtl(ObservableMap<AppStateType, AppStateData> appState, MainSrv service) {
        this.service = service;
        this.appState = appState;
        this.appState.addListener(
                (MapChangeListener<AppStateType, AppStateData>) change -> {
                    if (AppStateType.USER_DETAILS.equals(change.getKey())) {
                        UserDetails userDetails = (UserDetails) change.getValueAdded().getValue();
                        Platform.runLater(()->lockControls(userDetails == UserDetails.NOT_SIGNED_USER));
                    }
                });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setting UI
        operatingDays = FXCollections.emptyObservableList();
        tblOperatingDays.setItems(operatingDays);
        tblOperatingDays.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        operatingDay.setCellValueFactory(cell -> cell.getValue().operatingDayProperty());
        exType.setCellValueFactory(cell -> cell.getValue().typeSuProperty());
        status.setCellValueFactory(cell -> cell.getValue().statusProperty());
        createdAt.setCellValueFactory(cell -> cell.getValue().createdAtProperty());
        edited.setCellValueFactory(cell -> cell.getValue().editedProperty());
    }

    @FXML public void onReload(ActionEvent ev) {
        service.findOperatingDaysAsync(period++)
                .thenAccept(l -> Platform.runLater(() -> display(l)) );
    }

    @FXML void onWrite(ActionEvent ev) {
        log.info("MainCtl.onWrite");
    }

    //region Private

    private void lockControls(boolean lock) {
        butReload.setDisable(lock);
        butWrite.setDisable(lock);
    }

    private void display(List<OperatingDayFX> operatingDays) {
        this.operatingDays = FXCollections.observableList(operatingDays);
        tblOperatingDays.setItems(this.operatingDays);
    }

    //endregion
}
