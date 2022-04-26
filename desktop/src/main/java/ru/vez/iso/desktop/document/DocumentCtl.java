package ru.vez.iso.desktop.document;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.shared.AppStateData;
import ru.vez.iso.desktop.shared.AppStateType;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

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
    @FXML private TableColumn<DocumentFX, CheckBox> selection;

    @FXML private CheckBox selectAll;
    @FXML private Button butOpenFile;
    @FXML private Button butCheckHash;
    @FXML private Button butSearchDocs;
    @FXML private Button butPrint;
    @FXML private Button butDownload;

    private final ObservableMap<AppStateType, AppStateData> appState;
    private ObservableList<DocumentFX> documents;
    private final List<CheckBox> checkBoxes = new ArrayList<>();
    private final DocumentSrv service;

    public DocumentCtl(ObservableMap<AppStateType, AppStateData> appState, DocumentSrv srv) {
        this.appState = appState;
        this.service = srv;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("initialize");
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

        selection.setCellValueFactory(this::createObservableDocCheckbox);

        // Add load data listener
        this.appState.addListener(
            (MapChangeListener<AppStateType, AppStateData>)
                change -> {
                  if (AppStateType.DOCUMENTS.equals(change.getKey())) {
                    List<DocumentFX> docs = (List<DocumentFX>) change.getValueAdded().getValue();
                    Platform.runLater(() -> displayData(docs));
                  }
                });

    }

    // open ChooseFile dialog and fire service to load from file
    @FXML void onOpenFile(ActionEvent ev) {
        logger.debug("DocumentCtl.onOpenFile");
        FileChooser chooseFile = new FileChooser();
        chooseFile.setInitialDirectory(Paths.get(System.getProperty("user.home")).toFile());
        chooseFile.getExtensionFilters().clear();
        chooseFile.getExtensionFilters().add(new FileChooser.ExtensionFilter("DEV.ZIP", "*.zip"));

        File file = chooseFile.showOpenDialog(null);
        if (file != null) {
            Path path = file.toPath();
            logger.debug("Choose: " + path);
            // if opened, launch service to read data
            service.loadAsync(path);
        }
    }

    @FXML public void onSelectAll(ActionEvent ev) {
        logger.debug( "DocumentCtl.onSelectAll: " + selectAll.isSelected() );
        this.checkBoxes.forEach( cbox -> cbox.setSelected(selectAll.isSelected()) );
    }

    @FXML void onDownload(ActionEvent ev) {
        logger.debug("DocumentCtl.onDownload");
    }
    @FXML void onSearchDocs(ActionEvent ev) {
        logger.debug("DocumentCtl.onSearchDocs");
    }
    @FXML void onWriteCopy(ActionEvent ev) {
        logger.debug("DocumentCtl.onWriteCopy");
    }
    @FXML void onPrint(ActionEvent ev) {
        logger.debug("DocumentCtl.onPrint");
    }
    @FXML void onCheckHash(ActionEvent ev) {
        logger.debug("DocumentCtl.onCheckHash");
    }

    //region Private

    /**
     * Связывает DocumentFX and CheckBox чтобы при изменении значения
     * checkBox, изменялось также значение DocFX
     *
     * @param cell - support class used in TableColumn as a wrapper class * to provide all necessary information for a particular Cell
     * @return ObservableValue<CheckBox>
     */
    private ObservableValue<CheckBox> createObservableDocCheckbox(
            TableColumn.CellDataFeatures<DocumentFX, CheckBox> cell) {
        DocumentFX doc = cell.getValue();
        CheckBox cbox = new CheckBox();
        cbox.selectedProperty().setValue(doc.isSelected());
        cbox.selectedProperty().addListener((ov, old, newVal) -> {
            logger.debug( (newVal ? "check" : "uncheck") + " docNum: " + doc.getDocNumber() );
            doc.setSelected(newVal);
            this.unlockDocumentButtonsIfAnySelected();
        });
        // save reference to the newly created checkbox for bulk operations
        checkBoxes.add(cbox);
        return new SimpleObjectProperty<>(cbox);
    }

    /**
     * Map list of documents to FXCollection and set to the TableView
     *
     * @param docs - list of documents
     * */
    private void displayData(List<DocumentFX> docs) {
        // remove all previously saved references to checkboxes
        this.checkBoxes.clear();
        // Set items to the tableView
        this.documents = FXCollections.observableList(docs);
        tblDocuments.setItems(this.documents);
        // Enable/disable disk-related buttons
        this.unlockDiskOpsButtons( docs.size()>0 );
        this.unlockDocumentButtonsIfAnySelected();
    }

    // Lock/Unlock all disk-related operations
    private void unlockDiskOpsButtons(boolean unlock) {
        selectAll.setDisable(!unlock);
        butSearchDocs.setDisable(!unlock);
        butCheckHash.setDisable(!unlock);
    }

    // lock/unlock document related operations
    private void unlockDocumentButtonsIfAnySelected() {
        boolean anySelected = documents.stream().anyMatch(DocumentFX::isSelected);
        butPrint.setDisable(!anySelected);
        butDownload.setDisable(!anySelected);
    }

    //endregion
}

