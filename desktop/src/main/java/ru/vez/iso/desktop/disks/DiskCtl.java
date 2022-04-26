package ru.vez.iso.desktop.disks;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.shared.SettingType;
import ru.vez.iso.desktop.shared.AppStateData;
import ru.vez.iso.desktop.shared.AppStateType;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DiskCtl implements Initializable {

    private static final Logger logger = LogManager.getLogger();

    @FXML private Button butCheck;
    @FXML private Button butReload;
    @FXML private Button butWriteCopy;

    @FXML private TableView<IsoFileFX> tblIsoFiles;
    @FXML private TableColumn<IsoFileFX, String> numberSu;
    @FXML private TableColumn<IsoFileFX, String> fileName;

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final DisksSrv service;
    private ObservableList<IsoFileFX> isoFilesFX;

    public DiskCtl(ObservableMap<AppStateType, AppStateData> appState, DisksSrv service) {
        this.appState = appState;
        this.service = service;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("initialize");

        // Setting UI
        this.isoFilesFX = FXCollections.emptyObservableList();
        this.tblIsoFiles.setItems(this.isoFilesFX);
        this.tblIsoFiles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Column settings
        numberSu.setCellValueFactory(cell -> cell.getValue().numberSuProperty());
        fileName.setCellValueFactory(cell -> cell.getValue().fileNameProperty());

        // disable buttons if none selected
        butWriteCopy.disableProperty().bind(
                tblIsoFiles.getSelectionModel().selectedItemProperty().isNull()
        );
        butCheck.disableProperty().bind(
                tblIsoFiles.getSelectionModel().selectedItemProperty().isNull()
        );

        // Add Data listener for ISO_FILES populated
        this.appState.addListener(
                (MapChangeListener<AppStateType, AppStateData>) change -> {
                    if (AppStateType.ISO_FILES_NAMES.equals(change.getKey())) {
                        List<IsoFileFX> data = (List<IsoFileFX>) change.getValueAdded().getValue();
                        Platform.runLater(()-> displayData(data));
                    }
                });

        // Reload file list when change in LOAD_ISO_STATUS
        this.appState.addListener(
                (MapChangeListener<AppStateType, AppStateData>) change -> {
                    if (AppStateType.LOAD_ISO_STATUS.equals(change.getKey())) {
                        onReload(null);
                    }
                });

        onReload(null);
    }

    @FXML public void onReload(ActionEvent ev) {
        logger.debug("onReload");
        service.readIsoFileNamesAsync(SettingType.DOWNLOAD_ISO_PATH.getDefaultValue());
    }

    @FXML public void onCheck(ActionEvent ev) {
        logger.debug("onCheck");
    }

    @FXML public void onWriteCopy(ActionEvent ev) {
        logger.debug("onWriteCopy");
    }


    //region Private

    private void displayData(List<IsoFileFX> data) {
        // Set items to the tableView
        this.isoFilesFX = FXCollections.observableList(data);
        tblIsoFiles.setItems(this.isoFilesFX);
    }



    //endregion

}
