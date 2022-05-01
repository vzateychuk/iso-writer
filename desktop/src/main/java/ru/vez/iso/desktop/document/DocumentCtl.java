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
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import ru.vez.iso.desktop.shared.AppStateData;
import ru.vez.iso.desktop.shared.AppStateType;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    @FXML private TableColumn<DocumentFX, CheckBox> selection;

    @FXML private CheckBox selectAll;
    @FXML private Button butOpenFile;
    @FXML private Button butCheckHash;
    @FXML private Button butFilter;
    @FXML private Button butPrint;
    @FXML private Button butDownload;
    @FXML private TextField txtFilter;

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

        selection.setCellValueFactory(this::createObservableDocCheckbox);

        // Add load data listener
        this.appState.addListener(
            (MapChangeListener<AppStateType, AppStateData>)
                change -> {
                  if (AppStateType.DOCUMENTS.equals(change.getKey())) {
                    List<DocumentFX> docs = (List<DocumentFX>) change.getValueAdded().getValue();
                    Platform.runLater(() -> filterAndDisplay(docs, txtFilter.getText()));
                  }
                });

    }

    // open ChooseFile dialog and fire service to load from file
    @FXML void onOpenFile(ActionEvent ev) {
        logger.debug("");
        FileChooser chooseFile = new FileChooser();
        chooseFile.setInitialDirectory(Paths.get(System.getProperty("user.home")).toFile());
        chooseFile.getExtensionFilters().clear();
        chooseFile.getExtensionFilters().add(new FileChooser.ExtensionFilter("DIR.ZIP", "*.zip"));

        File file = chooseFile.showOpenDialog(null);
        if (file != null) {
            Path path = file.toPath();
            logger.debug("Choose: " + path);
            // if opened, launch service to read data
            service.loadAsync(path);
        }
    }

    @FXML public void onSelectAll(ActionEvent ev) {
        logger.debug( selectAll.isSelected() );
        this.checkBoxes.forEach( cbox -> cbox.setSelected(selectAll.isSelected()) );
    }

    @FXML void onDownload(ActionEvent ev) {
        logger.debug("");
    }
    @FXML void onFilter(ActionEvent ev) {
        logger.debug("");
        AppStateData<List<DocumentFX>> data = (AppStateData<List<DocumentFX>>) appState.get(AppStateType.DOCUMENTS);
        filterAndDisplay(data.getValue(), txtFilter.getText());
    }
    @FXML public void onFilterEnter(KeyEvent ke) {
        if( ke.getCode() == KeyCode.ENTER ) {
            onFilter(null);
        }
    }
    @FXML void onWriteCopy(ActionEvent ev) {
        logger.debug("");
    }
    @FXML void onPrint(ActionEvent ev) {
        logger.debug("");
    }
    @FXML void onCheckHash(ActionEvent ev) {
        logger.debug("");
    }

    //region Private

    /**
     * Связывает DocumentFX and CheckBox чтобы при изменении значения
     * checkBox, изменялось также значение DocFX
     *
     * @param cell - support class used in TableColumn as a wrapper class * to provide all necessary information for a particular Cell
     * @return ObservableValue<CheckBox>
     */
    private ObservableValue<CheckBox> createObservableDocCheckbox(TableColumn.CellDataFeatures<DocumentFX, CheckBox> cell) {

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
     * Filter list of documents and display
     * (set to the TableView)
     *
     * @param docs - list of documents
     * @param filter
     * */
    private void filterAndDisplay(List<DocumentFX> docs, String filter) {
        // remove all previously saved references to checkboxes
        this.checkBoxes.clear();

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
        this.unlockDiskOpsButtons( filtered.size()>0 );
        this.unlockDocumentButtonsIfAnySelected();
    }

    // Lock/Unlock all disk-related operations
    private void unlockDiskOpsButtons(boolean unlock) {
        selectAll.setDisable(!unlock);
        butFilter.setDisable(!unlock);
        butCheckHash.setDisable(!unlock);
        txtFilter.setDisable(!unlock);
        butFilter.setDisable(!unlock);
    }

    // lock/unlock document related operations
    private void unlockDocumentButtonsIfAnySelected() {
        boolean anySelected = documents.stream().anyMatch(DocumentFX::isSelected);
        butPrint.setDisable(!anySelected);
        butDownload.setDisable(!anySelected);
    }

    //endregion
}

