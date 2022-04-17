package ru.vez.iso.desktop.document;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Controller for Document view
 * */
public class DocumentCtl {

    @FXML private TableView<?> tblDocuments;
    @FXML private TableColumn<?, ?> branch;
    @FXML private TableColumn<?, ?> docDate;
    @FXML private TableColumn<?, ?> docNumber;
    @FXML private TableColumn<?, ?> docStatusName;
    @FXML private TableColumn<?, ?> kindIdName;
    @FXML private TableColumn<?, ?> operDayDate;
    @FXML private TableColumn<?, ?> selection;
    @FXML private TableColumn<?, ?> sumDoc;

    @FXML private Button butPrint;
    @FXML private Button butSearch;
    @FXML private Button butCheckHash2;
    @FXML private Button butSearchDocs2;
    @FXML private Button butWriteCopy2;

    private final DocumentSrv documentSrv;

    public DocumentCtl(DocumentSrv srv) {
        this.documentSrv = srv;
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

}
