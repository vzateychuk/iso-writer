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
import ru.vez.iso.desktop.state.AppStateData;
import ru.vez.iso.desktop.state.AppStateType;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for Document view
 * */
public class DocumentCtl implements Initializable {

    @FXML private TableView<DocumentFX> tblDocuments;
    @FXML private TableColumn<DocumentFX, BranchType> branch;
    @FXML private TableColumn<DocumentFX, LocalDate> docDate;
    @FXML private TableColumn<DocumentFX, String> docNumber;
    @FXML private TableColumn<DocumentFX, DocStatus> docStatusName;
    @FXML private TableColumn<DocumentFX, DocType> kindIdName;
    @FXML private TableColumn<DocumentFX, LocalDate> operDayDate;
    @FXML private TableColumn<DocumentFX, Double> sumDoc;
    @FXML private TableColumn<DocumentFX, CheckBox> selection;

    @FXML private CheckBox selectAll;
    @FXML private Button butOpenFile;
    @FXML private Button butCheckHash;
    @FXML private Button butSearchDocs;
    @FXML private Button butPrint;
    @FXML private Button butDownload;
    @FXML private Button butWriteCopy;
    @FXML private Button butCheckHash2;
    @FXML private Button butSearchDocs2;
    @FXML private Button butWriteCopy2;

    private final ObservableMap<AppStateType, AppStateData> appState;
    private ObservableList<DocumentFX> documents;
    private final DocumentSrv service;

    public DocumentCtl(ObservableMap<AppStateType, AppStateData> appState, DocumentSrv srv) {
        this.appState = appState;
        this.service = srv;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
                  System.out.println("DocumentCtl.initialize: AppState change: " + change.getKey());
                  if (AppStateType.DOCUMENTS.equals(change.getKey())) {
                    List<DocumentFX> docs = (List<DocumentFX>) change.getValueAdded().getValue();
                    Platform.runLater(() -> displayData(docs));
                  }
                });

    }

    // open ChooseFile dialog and fire service to load from file
    @FXML void onOpenFile(ActionEvent ev) {
        System.out.println("DocumentCtl.onOpenFile");
        FileChooser chooseFile = new FileChooser();
        chooseFile.setInitialDirectory(Paths.get(System.getProperty("user.home")).toFile());
        // TODO extension filter doesn't work
        chooseFile.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("DEV.ZIP", "*.zip"));
        File file = chooseFile.showOpenDialog(null);
        if (file != null) {
            Path path = file.toPath();
            System.out.println("Choose: " + path);
            // if opened, launch service to read data
            service.loadAsync(path);
        }
    }

    @FXML public void onSelectAll(ActionEvent ev) {
        System.out.println("DocumentCtl.onSelectAll: " + selectAll.isSelected());
        documents.forEach( d -> d.setSelected(selectAll.isSelected()));
    }

    @FXML void onDownload(ActionEvent ev) {
        System.out.println("DocumentCtl.onDownload");
    }
    @FXML void onSearchDocs(ActionEvent ev) {
        System.out.println("DocumentCtl.onSearchDocs");
    }
    @FXML void onWriteCopy(ActionEvent ev) {
        System.out.println("DocumentCtl.onWriteCopy");
    }
    @FXML void onPrint(ActionEvent ev) {
        System.out.println("DocumentCtl.onPrint");
    }
    @FXML void onCheckHash(ActionEvent ev) {
        System.out.println("DocumentCtl.onCheckHash");
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
        CheckBox checkBox = new CheckBox();
        checkBox.selectedProperty().setValue(doc.isSelected());
        checkBox.selectedProperty().addListener((ov, old, newVal) -> {
            System.out.println( (newVal ? "check" : "uncheck") + " docNum: " + doc.getDocNumber() );
            doc.setSelected(newVal);
            this.unlockDocumentButtonsIfAnySelected();
        });
        return new SimpleObjectProperty<>(checkBox);
    }

    /**
     * Map list of documents to FXCollection and set to the TableView
     *
     * @param docs - list of documents
     * */
    private void displayData(List<DocumentFX> docs) {
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
        butSearchDocs2.setDisable(!unlock);
        butWriteCopy.setDisable(!unlock);
        butWriteCopy2.setDisable(!unlock);
        butCheckHash.setDisable(!unlock);
        butCheckHash2.setDisable(!unlock);
    }

    // lock/unlock document related operations
    private void unlockDocumentButtonsIfAnySelected() {
        boolean anySelected = documents.stream().anyMatch(DocumentFX::isSelected);
        butPrint.setDisable(!anySelected);
        butDownload.setDisable(!anySelected);
    }

    //endregion
}

