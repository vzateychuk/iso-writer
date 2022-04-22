package ru.vez.iso.desktop.disks;

import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.settings.SettingType;
import ru.vez.iso.desktop.state.AppStateData;
import ru.vez.iso.desktop.state.AppStateType;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DiskCtl implements Initializable {

    private static Logger logger = LogManager.getLogger();

    @FXML private Button butCheck;
    @FXML private Button butReload;
    @FXML private Button butWriteCopy;

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final DisksSrv service;

    public DiskCtl(ObservableMap<AppStateType, AppStateData> appState, DisksSrv service) {
        this.appState = appState;
        this.service = service;

        // Add Data listener for ISO_FILES populated
        this.appState.addListener(
                (MapChangeListener<AppStateType, AppStateData>) change -> {
                    if (AppStateType.ISO_FILES.equals(change.getKey())) {
                        List<String> data = (List<String>) change.getValueAdded().getValue();
                        Platform.runLater(()-> displayIsoFiles(data));
                    }
                });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("initialize");
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

    private void displayIsoFiles(List<String> fileNames) {
        logger.debug("displayIsoFiles: " + fileNames);
    }

    //endregion

}
