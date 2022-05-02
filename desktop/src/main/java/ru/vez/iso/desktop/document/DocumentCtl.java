package ru.vez.iso.desktop.document;

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
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import ru.vez.iso.desktop.document.reestr.RFileType;
import ru.vez.iso.desktop.document.reestr.Reestr;
import ru.vez.iso.desktop.document.reestr.ReestrDoc;
import ru.vez.iso.desktop.document.reestr.ReestrFile;
import ru.vez.iso.desktop.shared.AppSettings;
import ru.vez.iso.desktop.shared.AppStateData;
import ru.vez.iso.desktop.shared.AppStateType;
import ru.vez.iso.desktop.shared.MyContants;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Controller for Document view
 * */
public class DocumentCtl implements Initializable {

    private static final Logger logger = LogManager.getLogger();

    @FXML private TableView<DocumentFX> tblDocuments;
    @FXML private TableColumn<DocumentFX, String> branch;
    @FXML private TableColumn<DocumentFX, String> docDate;
    @FXML private TableColumn<DocumentFX, String> docNumber;
    @FXML private TableColumn<DocumentFX, String> docStatusName;
    @FXML private TableColumn<DocumentFX, String> kindIdName;
    @FXML private TableColumn<DocumentFX, String> operDayDate;
    @FXML private TableColumn<DocumentFX, Double> sumDoc;

    @FXML private Button butOpenZip;
    @FXML private Button butCheckHash;
    @FXML private Button butFilter;
    @FXML private Button butOpen;
    @FXML public Button butExplore;
    @FXML private TextField txtFilter;

    private final ObservableMap<AppStateType, AppStateData> appState;
    private ObservableList<DocumentFX> documents;
    private final DocumentSrv service;

    public DocumentCtl(ObservableMap<AppStateType, AppStateData> appState, DocumentSrv srv) {
        this.appState = appState;
        this.service = srv;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        logger.debug("");
        // Setting UI
        documents = FXCollections.emptyObservableList();
        tblDocuments.setItems(documents);
        tblDocuments.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Column settings
        branch.setCellValueFactory(cell -> cell.getValue().branchProperty());
        docDate.setCellValueFactory(cell -> cell.getValue().docDateProperty());
        docNumber.setCellValueFactory(cell -> cell.getValue().docNumberProperty());
        docStatusName.setCellValueFactory(cell -> cell.getValue().docStatusNameProperty());
        kindIdName.setCellValueFactory(cell -> cell.getValue().docTypeProperty());
        operDayDate.setCellValueFactory(cell -> cell.getValue().operDayDateProperty());
        sumDoc.setCellValueFactory(cell -> cell.getValue().sumDocProperty());

        // Add load data listener
        this.appState.addListener(
            (MapChangeListener<AppStateType, AppStateData>)
                change -> {
                  if (AppStateType.DOCUMENTS.equals(change.getKey())) {
                    List<DocumentFX> docs = (List<DocumentFX>) change.getValueAdded().getValue();
                    Platform.runLater(() -> filterAndDisplay(docs, txtFilter.getText()));
                  }
                });

        // disable buttons if no record selected
        butOpen.disableProperty().bind( tblDocuments.getSelectionModel().selectedItemProperty().isNull() );
        butExplore.disableProperty().bind( tblDocuments.getSelectionModel().selectedItemProperty().isNull() );
    }

    // open ChooseFile dialog and fire service to load documents from ZIP
    @FXML void onOpenZip(ActionEvent ev) {

        FileChooser chooseFile = new FileChooser();
        chooseFile.setInitialDirectory(Paths.get(System.getProperty("user.home")).toFile());
        chooseFile.getExtensionFilters().clear();
        chooseFile.getExtensionFilters().add(new FileChooser.ExtensionFilter("DIR.ZIP", "*.zip"));

        File chosen = chooseFile.showOpenDialog(null);
        if (chosen != null) {
            txtFilter.setText("");
            // if opened, launch service to read data
            Path path = chosen.toPath();
            logger.debug("ZIP path: " + path);
            service.loadAsync(path);
        }
    }

    // open a document's file natively
    @FXML void onOpen(ActionEvent ev) {

        logger.debug("");
        final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop == null || !desktop.isSupported(Desktop.Action.OPEN)) {
            logger.warn("action not supported");
            return;
        }

        DocumentFX doc = tblDocuments.getSelectionModel().getSelectedItem();
        Reestr reestr = ((AppStateData<Reestr>) appState.get(AppStateType.REESTR)).getValue();
        ReestrDoc reestrDoc = reestr.getDocs().stream().filter(d -> d.getData().getObjectId().equals(doc.getObjectId())).findAny()
                    .orElseThrow(() -> new RuntimeException("not found in REESTR, exit: " + doc.getObjectId()));
        ReestrFile file = reestrDoc.getFiles().stream()
                    .filter(f -> f.getType().equals(RFileType.PF)).findAny()
                    .orElseThrow(() -> new RuntimeException(RFileType.PF.getTitle() + " not found in REESTR, exit"));
        AppSettings sets = ((AppStateData<AppSettings>) appState.get(AppStateType.SETTINGS)).getValue();
        Path unzippedPath = Paths.get(sets.getIsoCachePath(), MyContants.UNZIP_FOLDER, doc.getObjectId(), file.getPath());

        logger.debug("open: {}", unzippedPath);
        try {
            desktop.open(unzippedPath.toFile());
        } catch (IOException ex) {
            logger.warn("unable to open {}", unzippedPath, ex);
        }
    }

    // open a folder with document natively
    @FXML public void onExplore(ActionEvent ev) {
        logger.debug("");
        final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop == null || !desktop.isSupported(Desktop.Action.OPEN)) {
            logger.warn("action not supported");
            return;
        }

        DocumentFX doc = tblDocuments.getSelectionModel().getSelectedItem();
        AppSettings sets = ((AppStateData<AppSettings>) appState.get(AppStateType.SETTINGS)).getValue();
        Path unzippedPath = Paths.get(sets.getIsoCachePath(), MyContants.UNZIP_FOLDER, doc.getObjectId());

        logger.debug("open: {}", unzippedPath);
        try {
            desktop.open(unzippedPath.toFile());
        } catch (IOException ex) {
            logger.warn("unable to open {}", unzippedPath, ex);
        }
    }

    @FXML void onFilter(ActionEvent ev) {

        logger.debug("");

        AppStateData<List<DocumentFX>> data = (AppStateData<List<DocumentFX>>) appState.get(AppStateType.DOCUMENTS);
        if (data != null) {
            this.filterAndDisplay(data.getValue(), txtFilter.getText());
        }
    }
    @FXML public void onFilterEnter(KeyEvent ke) {
        if( ke.getCode() == KeyCode.ENTER ) {
            onFilter(null);
        }
    }
    @FXML void onCheckHash(ActionEvent ev) {
        logger.debug("");
    }

    //region Private

    /**
     * Filter list of documents and display
     * (set to the TableView)
     *
     * @param docs - list of documents
     * @param filter
     * */
    private void filterAndDisplay(List<DocumentFX> docs, String filter) {

        if (docs.size()==0) {
            logger.debug("no records, return");
            return;
        }

        Predicate<DocumentFX> filterDoc = d ->
                !Strings.isBlank(d.getDocNumber()) && d.getDocNumber().toLowerCase().contains(filter.toLowerCase())
                  || d.getDocType().getTitle().toLowerCase().contains(filter.toLowerCase())
                  || d.getBranch().getTitle().toLowerCase().contains(filter.toLowerCase())
                  || d.getDocStatusName().getTitle().toLowerCase().contains(filter.toLowerCase());
        // Set items to the tableView
        List<DocumentFX> filtered = Strings.isBlank(filter) ? docs : docs.stream().filter(filterDoc).collect(Collectors.toList());

        this.documents = FXCollections.observableList(filtered);
        tblDocuments.setItems(this.documents);
        // Enable/disable disk-related buttons
        butCheckHash.setDisable(filtered.size()==0);
    }

    //endregion
}

