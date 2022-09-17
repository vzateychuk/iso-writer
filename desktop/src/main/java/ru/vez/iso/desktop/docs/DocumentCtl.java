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
import ru.vez.iso.desktop.docs.reestr.ReestrFile;
import ru.vez.iso.desktop.helper.ReestrHelper;
import ru.vez.iso.desktop.main.storeunits.StorageUnitsService;
import ru.vez.iso.desktop.shared.*;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
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
    @FXML private TableColumn<DocumentFX, String> kindIdName;
    @FXML private TableColumn<DocumentFX, String> operDayDate;
    @FXML private TableColumn<DocumentFX, String> sumDoc;

    @FXML private Button butOpenZip;
    @FXML private Button butCheckSumDisk;
    @FXML private Button butCheckSumDoc;
    @FXML private Button butFilter;
    @FXML private Button butViewDoc;
    @FXML public Button butExploreDoc;
    @FXML private TextField txtFilter;

    private final ApplicationState state;
    private ObservableList<DocumentFX> documents;
    private final DocSrv docSrv;
    private final MessageSrv msgSrv;
    private final StorageUnitsService storageUnitsSrv;
    private final Executor exec;

    // State of current asyncOperation
    private Future<Void> asyncOperation = CompletableFuture.allOf();

    public DocumentCtl(ApplicationState appState,
                       DocSrv srv,
                       MessageSrv msgSrv,
                       StorageUnitsService suSrv,
                       Executor exec) {
        this.state = appState;
        this.docSrv = srv;
        this.msgSrv = msgSrv;
        this.storageUnitsSrv = suSrv;
        this.exec = exec;
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
    @FXML public void onOpenZip(ActionEvent ev) {

        // Avoid multiply invocation
        if (!asyncOperation.isDone()) {
            this.msgSrv.news("Выполняется операция, подождите.");
            logger.debug("Async operation in progress, return");
            return;
        }

        FileChooser chooseFile = new FileChooser();
        chooseFile.setInitialDirectory(Paths.get(System.getProperty("user.home")).toFile());
        chooseFile.getExtensionFilters().clear();
        chooseFile.getExtensionFilters().add(new FileChooser.ExtensionFilter(MyConst.DIR_ZIP_FILE, "*.zip"));

        File chosen = chooseFile.showOpenDialog(null);

        // if opened, save as current directory and launch service to read data
        if (chosen == null) {
            logger.debug("DirZip file is not chosen, return");
            return;
        }

        txtFilter.setText("");
        Path dirZip = chosen.toPath();
        this.msgSrv.news(String.format("Открывается файл %s.", dirZip));

        state.setZipDir( dirZip.getParent().toString() );

        logger.debug("Opening: {}", dirZip);

        asyncOperation = CompletableFuture.runAsync( () -> {
            List<DocumentFX> docs = docSrv.loadREESTR(dirZip);
            state.setDocumentFXs(docs);
            this.msgSrv.news(String.format("Открыт файл %s.", dirZip));
        }, exec)
        .exceptionally( ex -> {
                    logger.error("Unable to load REESTR from: {}", dirZip, ex);
                    this.msgSrv.news(String.format("Ошибка чтения %s", dirZip));
                    return null;
        });
    }

    // open a document's file natively
    @FXML public void onViewDoc(ActionEvent ev) {

        final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop == null || !desktop.isSupported(Desktop.Action.OPEN)) {
            logger.warn("action not supported");
            return;
        }

        // Save document from the Reestr to file-cache first
        DocumentFX doc = tblDocuments.getSelectionModel().getSelectedItem();
        logger.debug("opening doc: {}", doc.getDocNumber());

        Reestr reestr = this.state.getReestr();
        ReestrFile pfFile = ReestrHelper.findFileInReestrOrException(reestr, doc.getObjectId(), RFileType.PF);
        AppSettings sets = this.state.getSettings();
        Path unzippedPath = Paths.get(sets.getIsoCachePath(), MyConst.UNZIP_FOLDER, doc.getObjectId(), pfFile.getPath());

        logger.debug("open file: {}", unzippedPath);

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
            logger.debug("Copied '{}' to and opened dir {}", doc.getDocNumber(), dest);
        } catch (IOException ex) {
            logger.warn("Unable to copy and open {}", unzippedDoc, ex);
            msgSrv.news(String.format("Ошибка копирования документа '%s' в %s", doc.getDocNumber(), dest));
        }
    }

    // filter dataset
    @FXML public void onFilter(ActionEvent ev) {

        logger.debug(this.txtFilter.getText());

        String filter = Strings.trimToNull(this.txtFilter.getText());

        if (!Strings.isBlank(filter) && filter.length()<3) {
            msgSrv.news("Для поиска необходимо ввести не менее 3 символов");
            return;
        }

        List<DocumentFX> docs = this.state.getDocumentFXs();
        if (docs != null && !docs.isEmpty() ) {
            this.filterAndDisplay(docs, filter);
        }
    }
    @FXML public void onFilterEnter(KeyEvent ke) {
        if( ke.getCode() == KeyCode.ENTER ) {
            onFilter(null);
        }
    }

    // check hashes for: DIR.zip, checksum.txt and server
    @FXML public void onCheckSumDisk(ActionEvent ev) {

        // Avoid multiply invocation
        if (!asyncOperation.isDone()) {
            this.msgSrv.news("Выполняется операция, подождите.");
            logger.debug("Async operation in progress, return");
            return;
        }

        // проверяем есть ли ZipDir с разархивированными документами
        String curPath = this.state.getZipDir();
        if (isBlank(curPath)) {
            this.msgSrv.news("Невозможно выполнить проверку контрольной суммы. Не открыт DIR.zip");
            logger.warn("DIR.zip path not defined, exit");
            return;
        }

        // проверяем доступны ли файл checksum.txt и DIR.zip
        Path checksumPath = Paths.get(curPath, MyConst.CHECKSUM_FILE);
        Path dirZipPath = Paths.get(curPath, MyConst.DIR_ZIP_FILE);
        if (!Files.exists(checksumPath) || !Files.exists(dirZipPath)) {
            this.msgSrv.news("Невозможно прочитать файлы «checksum.txt», «DIR.zip». Файлы повреждены");
            logger.warn("Not exists {} or {}, exit", MyConst.CHECKSUM_FILE, MyConst.DIR_ZIP_FILE );
            return;
        }

        // проверяем есть ли подключение
        boolean authenticated = this.state.getUserDetails() != UserDetails.NOT_SIGNED_USER;
        String warn = "Авторизация в Подсистеме не выполнена. Проверка целостности диска будет произведена без обращения к Подсистеме";
        if (!authenticated && !UtilsHelper.getConfirmation(warn)) {
            this.msgSrv.news("Проверка контрольной суммы прервана.");
            logger.debug("Operation cancelled by user");
            return;
        }

        this.msgSrv.news("Производится расчет контрольной суммы файла, пожалуйста подождите");

        // Асинхронно получаем значения hash из checksum.txt, Dir.zip, backend и сверяем их
        // шаг 1. прочитать значение хэш в файле checksum.txt
        CompletableFuture<String> readCheckSumTxtHash = CompletableFuture.supplyAsync(
                ()->docSrv.readFile(checksumPath),
                exec
        );
        // шаг 2. рассчитать значение хэш открытого файла Dir.zip
        CompletableFuture<String> calcDirZipHash = CompletableFuture.supplyAsync(
                ()->docSrv.calculateFileHash(dirZipPath, MyConst.ALGO_GOST),
                exec
        );
        // шаг 3. получить значение хэша с сервера
        CompletableFuture<String> requestBackendHash = CompletableFuture.supplyAsync(
                ()-> authenticated ? storageUnitsSrv.getHashValue(this.state.getReestr().getStorageUnitId()) : "",
                exec
        );
        // параллельно выполняем шаги, вычисляем и проверяем совпадение значений HASH
        asyncOperation = CompletableFuture.allOf( readCheckSumTxtHash, calcDirZipHash, requestBackendHash)
                .thenAccept( (Void) -> {
                    String checksumTxtHash = readCheckSumTxtHash.join();
                    String dirZipHash = calcDirZipHash.join();
                    String serverHash = requestBackendHash.join();
                    logger.debug("Hash check.\nchecksum.txt:\t{}\nDir.zip     :\t{}\nServer      :\t{}",
                            checksumTxtHash, dirZipHash, serverHash);
                    String msg = "Проверка целостности диска выполнена. ";
                    if (dirZipHash.equals(checksumTxtHash) && (!authenticated || dirZipHash.equals(serverHash))) {
                        msg += "HASH-суммы единицы хранения совпадают.";
                    } else {
                        msg += "Обнаружены несовпадения хэш-сумм.";
                    }
                    this.msgSrv.news(msg);
                })
                .exceptionally( ex -> {
                    logger.error("Unable to compare HashSum", ex);
                    this.msgSrv.news("Ошибка при проверке целостности диска");
                    return null;
                });
    }

    // check hashes for json file and REESTR value
    @FXML public void onCheckSumDoc(ActionEvent ev) {

        DocumentFX doc = tblDocuments.getSelectionModel().getSelectedItem();
        String cachePath = state.getSettings().getIsoCachePath();
        // find the document in reestr
        Reestr reestr = this.state.getReestr();
        ReestrFile jsonFile = ReestrHelper.findFileInReestrOrException(reestr, doc.getObjectId(), RFileType.JSON);
        Path jsonFilePath = Paths.get(cachePath, MyConst.UNZIP_FOLDER, doc.getObjectId(), jsonFile.getPath());
        logger.debug("jsonFilePath: {}", jsonFilePath);

        // проверяем есть ли json файл в кэш фактически
        if (!Files.exists(jsonFilePath)) {
            this.msgSrv.news("Невозможно выполнить проверку контрольной суммы документа. JSON файл не найден.");
            logger.error("file not found, exit: {}", jsonFilePath);
            return;
        }

        try {
            String expectedHash = jsonFile.getHash();
            String jsonFileHash = docSrv.calculateFileHash(jsonFilePath, MyConst.SHA256);

            logger.debug("Document #'{}'(id={}) hash checking.\nReestr   hash:\t{}\nJsonFile hash:\t{}",
                    doc.getDocNumber(), doc.getObjectId(), expectedHash, jsonFileHash);

            String msg = "Проверка целостности документа выполнена";
            if (jsonFileHash.equals(expectedHash)) {
                msg += " успешно.";
            } else {
                msg += ". Обнаружены несовпадения хэш-сумм.";
            }
            UtilsHelper.getConfirmation(msg);
        } catch (Exception ex) {
            logger.error("unable to check hash", ex);
            this.msgSrv.news("Ошибка при проверке контрольной суммы документа.");
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

        if (docs.isEmpty()) {
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
        butCheckSumDisk.setDisable(filtered.isEmpty());
        butCheckSumDoc.setDisable(filtered.isEmpty());
    }

    //endregion
}

