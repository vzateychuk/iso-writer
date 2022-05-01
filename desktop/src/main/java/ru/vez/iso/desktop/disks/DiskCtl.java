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
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import ru.vez.iso.desktop.shared.AppStateData;
import ru.vez.iso.desktop.shared.AppStateType;
import ru.vez.iso.desktop.shared.IsoFileFX;
import ru.vez.iso.desktop.shared.SettingType;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DiskCtl implements Initializable {

    private static final Logger logger = LogManager.getLogger();

    @FXML private Button butCheck;
    @FXML private Button butReload;
    @FXML private Button butWriteCopy;
    @FXML private Button butDelete;
    @FXML private TextField txtFilter;

    @FXML private TableView<IsoFileFX> tblIsoFiles;
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
        logger.debug("");

        // Setting UI
        this.isoFilesFX = FXCollections.emptyObservableList();
        this.tblIsoFiles.setItems(this.isoFilesFX);
        this.tblIsoFiles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Column settings
        fileName.setCellValueFactory(cell -> cell.getValue().fileNameProperty());

        // disable buttons if none selected
        butWriteCopy.disableProperty().bind( tblIsoFiles.getSelectionModel().selectedItemProperty().isNull() );
        butCheck.disableProperty().bind( tblIsoFiles.getSelectionModel().selectedItemProperty().isNull() );
        butDelete.disableProperty().bind( tblIsoFiles.getSelectionModel().selectedItemProperty().isNull() );

        // Add Data listener for ISO_FILES populated/changed
        this.appState.addListener(
                (MapChangeListener<AppStateType, AppStateData>) change -> {
                    if (AppStateType.ISO_FILES_NAMES.equals(change.getKey())) {
                        List<IsoFileFX> data = (List<IsoFileFX>) change.getValueAdded().getValue();
                        Platform.runLater(()-> filterAndDisplay(data, txtFilter.getText()));
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
        logger.debug("");
        service.readIsoFileNamesAsync(SettingType.ISO_CACHE_PATH.getDefaultValue());
    }

    @FXML public void onFilter(ActionEvent ev) {
        logger.debug("");
        AppStateData<List<IsoFileFX>> data = (AppStateData<List<IsoFileFX>>) appState.get(AppStateType.ISO_FILES_NAMES);
        filterAndDisplay(data.getValue(), txtFilter.getText());
    }
    @FXML public void onFilterEnter(KeyEvent ke) {
        if( ke.getCode() == KeyCode.ENTER ) {
            onFilter(null);
        }
    }

    @FXML public void onCheck(ActionEvent ev) {
        logger.debug("");
    }

    @FXML public void onWriteCopy(ActionEvent ev) {
        logger.debug("");
    }

    @FXML public void onDelete(ActionEvent ev) {
        logger.debug("");
        IsoFileFX fileFX = tblIsoFiles.getSelectionModel().selectedItemProperty().getValue();
        service.deleteFileAndReload(fileFX.getFileName());
    }

    //region Private

    /**
     * Filter fileList and display
     * */
    private void filterAndDisplay(List<IsoFileFX> fileList, String filter) {
        // Set items to the tableView
        final List<IsoFileFX> filtered = Strings.isBlank(filter) ? fileList
                : fileList.stream()
                        .filter(f -> f.getFileName().toLowerCase().contains(filter.toLowerCase()))
                        .collect(Collectors.toList());
        this.isoFilesFX = FXCollections.observableList(filtered);
        tblIsoFiles.setItems(this.isoFilesFX);
    }

    //endregion

}
