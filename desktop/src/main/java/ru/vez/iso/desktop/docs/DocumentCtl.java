package ru.vez.iso.desktop.docs;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import ru.vez.iso.desktop.docs.reestr.RFileType;
import ru.vez.iso.desktop.docs.reestr.Reestr;
import ru.vez.iso.desktop.docs.reestr.ReestrDoc;
import ru.vez.iso.desktop.docs.reestr.ReestrFile;
import ru.vez.iso.desktop.shared.AppSettings;
import ru.vez.iso.desktop.shared.MessageSrv;
import ru.vez.iso.desktop.shared.MyConst;
import ru.vez.iso.desktop.state.ApplicationState;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.isBlank;

/**
 * Controller for Document view
 * */
public class DocumentCtl implements Initializable {

    private static final Logger logger = LogManager.getLogger();

    @FXML private TableView<DocumentFX> tblDocuments;
    @FXML private TableColumn<DocumentFX, String> branch;
    @FXML private TableColumn<DocumentFX, String> docDate;
    @FXML private TableColumn<DocumentFX, String> docNumber;
    // @FXML private TableColumn<DocumentFX, String> docStatusName;
    @FXML private TableColumn<DocumentFX, String> kindIdName;
    @FXML private TableColumn<DocumentFX, String> operDayDate;
    @FXML private TableColumn<DocumentFX, String> sumDoc;

    @FXML private Button butOpenZip;
    @FXML private Button butCheckSum;
    @FXML private Button butFilter;
    @FXML private Button butViewDoc;
    @FXML public Button butExploreDoc;
    @FXML private TextField txtFilter;

    private final ApplicationState state;
    private ObservableList<DocumentFX> documents;
    private final DocSrv docSrv;
    private final MessageSrv msgSrv;

    public DocumentCtl(ApplicationState appState, DocSrv srv, MessageSrv msgSrv) {
        this.state = appState;
        this.docSrv = srv;
        this.msgSrv = msgSrv;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        logger.debug(location);
        // Setting UI
        documents = FXCollections.emptyObservableList();
        tblDocuments.setItems(documents);
        tblDocuments.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Column settings
        branch.setCellValueFactory(cell -> cell.getValue().branchNameProperty());
        docDate.setCellValueFactory(cell -> cell.getValue().docDateProperty());
        docNumber.setCellValueFactory(cell -> cell.getValue().docNumberProperty());
        // docStatusName.setCellValueFactory(cell -> cell.getValue().docStatusNameProperty());
        kindIdName.setCellValueFactory(cell -> cell.getValue().kindNameProperty());
        operDayDate.setCellValueFactory(cell -> cell.getValue().operDayDateProperty());
        sumDoc.setCellValueFactory(cell -> cell.getValue().sumDocProperty());

        // Add load data listener
        this.state.documentFXsProperty().addListener(
                (ob, oldVal, newVal) -> Platform.runLater(() -> filterAndDisplay(newVal, txtFilter.getText()))
        );

        // disable buttons if no record selected
        butViewDoc.disableProperty().bind( tblDocuments.getSelectionModel().selectedItemProperty().isNull() );
        butExploreDoc.disableProperty().bind( tblDocuments.getSelectionModel().selectedItemProperty().isNull() );
    }

    // open ChooseFile dialog and fire service to load documents from ZIP
    @FXML void onOpenZip(ActionEvent ev) {

        FileChooser chooseFile = new FileChooser();
        chooseFile.setInitialDirectory(Paths.get(System.getProperty("user.home")).toFile());
        chooseFile.getExtensionFilters().clear();
        chooseFile.getExtensionFilters().add(new FileChooser.ExtensionFilter(MyConst.DIR_ZIP, "*.zip"));

        File chosen = chooseFile.showOpenDialog(null);

        // if opened, save as current directory and launch service to read data
        if (chosen == null) {
            return;
        }

        txtFilter.setText("");
        Path dirZip = chosen.toPath();
        state.setZipDir( dirZip.getParent().toString() );
        logger.debug("Open: {}", dirZip);
        docSrv.loadAsync(dirZip);
    }

    // open a document's file natively
    @FXML void onViewDoc(ActionEvent ev) {

        logger.debug("");

        final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop == null || !desktop.isSupported(Desktop.Action.OPEN)) {
            logger.warn("action not supported");
            return;
        }

        // Save document from the Reestr to file-cache first
        DocumentFX doc = tblDocuments.getSelectionModel().getSelectedItem();
        Reestr reestr = this.state.getReestr();
        ReestrDoc reestrDoc = reestr.getDocs().stream()
                .filter(d -> d.getData().getObjectId().equals(doc.getObjectId()))
                .findAny()
                .orElseThrow(() -> new RuntimeException("not found in REESTR, exit: " + doc.getObjectId()));
        ReestrFile file = reestrDoc.getFiles().stream()
                .filter(f -> f.getType().equals(RFileType.PF))
                .findAny()
                .orElseThrow(() -> new RuntimeException(RFileType.PF.getTitle() + " not found in REESTR, exit"));
        AppSettings sets = this.state.getSettings();
        Path unzippedPath = Paths.get(sets.getIsoCachePath(), MyConst.UNZIP_FOLDER, doc.getObjectId(), file.getPath());

        logger.debug("open: {}", unzippedPath);
        try {
            desktop.open(unzippedPath.toFile());
        } catch (IOException ex) {
            logger.warn("unable to open {}", unzippedPath, ex);
        }
    }

    // open a folder with document natively
    @FXML public void onExploreDocs(ActionEvent ev) {

        logger.debug("");
        final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop == null || !desktop.isSupported(Desktop.Action.OPEN)) {
            logger.warn("action not supported");
            return;
        }

        // Choose a target directory
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setInitialDirectory(Paths.get(System.getProperty("user.home")).toFile());
        dirChooser.setTitle("Скачать документ в папку");

        File destFile = dirChooser.showDialog(null);
        logger.debug("user chose dir: {}", destFile);

        // if opened, save as current directory and launch service to read data
        if (destFile == null) {
            return;
        }

        // Get source directory (unzipped) from file-cache
        DocumentFX doc = tblDocuments.getSelectionModel().getSelectedItem();
        AppSettings sets = this.state.getSettings();
        Path unzippedDoc = Paths.get(sets.getIsoCachePath(), MyConst.UNZIP_FOLDER, doc.getObjectId());
        Path dest = Paths.get(destFile.toString(), doc.getObjectId());

        try {
            FileUtils.copyDirectory(unzippedDoc.toFile(), dest.toFile());
            desktop.open(dest.toFile());
            logger.debug(String.format("Copied '%s' to and opened dir %s", doc.getDocNumber(), dest));
        } catch (IOException ex) {
            logger.warn("Unable to copy and open {}", unzippedDoc, ex);
            msgSrv.news(String.format("Ошибка копирования документа '%s' в %s", doc.getDocNumber(), dest));
        }
    }

    @FXML void onFilter(ActionEvent ev) {

        logger.debug(this.txtFilter.getText());

        String filter = Strings.trimToNull(this.txtFilter.getText());

        if (!Strings.isBlank(filter) && filter.length()<3) {
            msgSrv.news("Для поиска необходимо ввести не менее 3 символов");
            return;
        }

        List<DocumentFX> docs = this.state.getDocumentFXs();
        if (docs != null && docs.size() > 0 ) {
            this.filterAndDisplay(docs, filter);
        }
    }
    @FXML public void onFilterEnter(KeyEvent ke) {
        if( ke.getCode() == KeyCode.ENTER ) {
            onFilter(null);
        }
    }

    // check hashes for DIR.zip and checksum
    @FXML void onCheckSum(ActionEvent ev) {

        String currentPath = this.state.getZipDir();
        if (isBlank(currentPath)) {
            this.msgSrv.news("Невозможно выполнить проверку контрольной суммы. Не открыт DIR.zip");
            logger.warn("DIR.zip path not defined, exit");
            return;
        }

        Path checksum = Paths.get(currentPath, "checksum.txt");
        Path dirZip = Paths.get(currentPath, MyConst.DIR_ZIP);
        if (Files.exists(checksum) && Files.exists(dirZip)) {
            logger.debug("Open: {}", checksum);
            String msg = docSrv.compareCheckSum(checksum, dirZip)
                        ? "HASH-суммы в checksum.txt и DIR.zip совпадают"
                        : "HASH-суммы в checksum.txt и DIR.zip различаются";
            this.msgSrv.news(msg);
        } else {
            logger.warn("Not found: '{}' or '{}'", checksum, dirZip);
            this.msgSrv.news("Не найден: " + checksum);
        }

    }

    //region PRIVATE

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

        List<DocumentFX> filtered;
        if (isBlank(filter)) {
            filtered = docs;
        } else {
            String filterLow = filter.toLowerCase();

            Predicate<DocumentFX> filterDoc = doc ->
                    !isBlank(doc.getDocNumber()) && doc.getDocNumber().toLowerCase().contains(filterLow)
                            || doc.getKindName().toLowerCase().contains(filterLow)
                            || doc.getBranchName().toLowerCase().contains(filterLow)
                            || doc.getDocStatusName().getTitle().toLowerCase().contains(filterLow)
                            || Double.toString(doc.getSumDoc()).contains(filterLow)
                            || doc.operDayDateProperty().get().contains(filterLow)
                            || doc.docDateProperty().get().contains(filterLow);
            filtered = docs.stream().filter(filterDoc).collect(Collectors.toList());
        }

        this.documents = FXCollections.observableList(filtered);
        tblDocuments.setItems(this.documents);
        // Enable/disable disk-related buttons
        butCheckSum.setDisable(filtered.size()==0);
    }

    //endregion
}

