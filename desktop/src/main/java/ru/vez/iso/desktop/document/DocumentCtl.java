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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ru.vez.iso.desktop.state.AppStateData;
import ru.vez.iso.desktop.state.AppStateType;

import java.net.URL;
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
    @FXML private TableColumn<DocumentFX, Boolean> selection;

    @FXML private Button butPrint;
    @FXML private Button butSearch;
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
        // set selection mode to only 1 row
        tblDocuments.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Column settings
        branch.setCellValueFactory(cell -> cell.getValue().branchProperty());
        docDate.setCellValueFactory(cell -> cell.getValue().docDateProperty());
        docNumber.setCellValueFactory(cell -> cell.getValue().docNumberProperty());
        docStatusName.setCellValueFactory(cell -> cell.getValue().docStatusNameProperty());
        kindIdName.setCellValueFactory(cell -> cell.getValue().docTypeProperty());
        operDayDate.setCellValueFactory(cell -> cell.getValue().operDayDateProperty());
        sumDoc.setCellValueFactory(cell -> cell.getValue().sumDocProperty());
        selection.setCellValueFactory(cell -> cell.getValue().selectedProperty());

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

        // Run load data
        onReload(null);
    }

    public void onReload(ActionEvent ev) {
        service.loadAsync();
    }

    @FXML void onSelectAll(ActionEvent ev) {
        System.out.println("DocumentCtl.onSelectAll");
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

    private void displayData(List<DocumentFX> docs) {
        this.documents = FXCollections.observableList(docs);
        tblDocuments.setItems(this.documents);
    }

    //endregion
}
