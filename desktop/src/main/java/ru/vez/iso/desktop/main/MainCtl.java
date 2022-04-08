package ru.vez.iso.desktop.main;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ru.vez.iso.desktop.model.ExStatus;
import ru.vez.iso.desktop.model.ExType;
import ru.vez.iso.desktop.model.OperatingDayFX;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
* Main controller for logged user
*/
public class MainCtl implements Initializable {

    @FXML private TableView<OperatingDayFX> tblOperatingDays;
    @FXML private TableColumn<OperatingDayFX, LocalDate> operatingDay;
    @FXML private TableColumn<OperatingDayFX, ExType> exType;
    @FXML private TableColumn<OperatingDayFX, ExStatus> status;
    @FXML private Button butCancel;
    @FXML private Button butSubmit;
    @FXML private Button butReload;

    private ObservableList<OperatingDayFX> operatingDays;
    private final MainSrv service;
    private int period = 1;

    public MainCtl(MainSrv service) {
        this.service = service;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // TODO: make periodDays configurable

        // Setting UI
        operatingDays = FXCollections.emptyObservableList();
        tblOperatingDays.setItems(operatingDays);
        tblOperatingDays.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        operatingDay.setCellValueFactory(cell -> cell.getValue().operatingDayProperty());
        exType.setCellValueFactory(cell -> cell.getValue().typeSuProperty());
        status.setCellValueFactory(cell -> cell.getValue().statusProperty());
    }

    @FXML public void onReload(ActionEvent ev) {
        service.findOperatingDaysAsync(period++)
                .thenAccept(l -> Platform.runLater(() -> display(l)) );
    }

    @FXML void onSubmit(ActionEvent ev) {
        System.out.println("MainCtl.onSubmit");
    }

    //region Private

    private void display(List<OperatingDayFX> operatingDays) {
        System.out.println("MainCtl.display: " + Thread.currentThread().getName());
        this.operatingDays = FXCollections.observableList(operatingDays);
        tblOperatingDays.setItems(this.operatingDays);
    }

    //endregion
}
