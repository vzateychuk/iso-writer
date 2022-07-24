package ru.vez.iso.desktop.main;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.extern.java.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import ru.vez.iso.desktop.burn.RecorderInfo;
import ru.vez.iso.desktop.main.operdays.OperatingDayFX;
import ru.vez.iso.desktop.main.storeunits.StorageUnitFX;
import ru.vez.iso.desktop.main.storeunits.StorageUnitStatus;
import ru.vez.iso.desktop.shared.*;
import ru.vez.iso.desktop.state.ApplicationState;
import ru.vez.iso.desktop.state.RunMode;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for: "Выбор Единицы хранения для записи на диск"
 * */
@Log
public class MainCtl implements Initializable {

    //region Properties

    private static final Logger logger = LogManager.getLogger();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Sort string representation of LocalDateTime columns
    private final Comparator<String> sortDateStrings = (s1, s2) -> {
        if (!Strings.isBlank(s1) && !Strings.isBlank(s2)) {
            LocalDate d1 = LocalDate.parse(s1, fmt);
            LocalDate d2 = LocalDate.parse(s2, fmt);
            return d1.compareTo(d2);
        } else if (Strings.isBlank(s1)) {
            return 1;
        } else {
            return -1;
        }
    };
    // Фильтр "Список операционных дней"
    @FXML private TextField operDaysFilter;

    // Таблица "Список операционных дней"
    @FXML private TableView<OperatingDayFX> tblOperatingDays;
    @FXML private TableColumn<OperatingDayFX, String> operatingDay;
    @FXML private TableColumn<OperatingDayFX, String> typeSu;
    @FXML private TableColumn<OperatingDayFX, String> status;
    @FXML private TableColumn<OperatingDayFX, String> createdAt;

    // Таблица "Список единиц хранения"
    @FXML private TableView<StorageUnitFX> tblStorageUnits;
    @FXML private TableColumn<StorageUnitFX, String> numberSu;
    @FXML private TableColumn<StorageUnitFX, String> creationDate;
    @FXML private TableColumn<StorageUnitFX, String> storageUnitStatus;
    @FXML private TableColumn<StorageUnitFX, String> savingDate;
    @FXML private TableColumn <StorageUnitFX, String> present;
    @FXML private TableColumn <StorageUnitFX, String> downloaded;

    // Кнопки
    @FXML private Button butIsoLoad;
    @FXML private Button butFileCacheRefresh;
    @FXML private Button butBurn;
    @FXML private Button butDelete;
    @FXML private Button butCheckSum;
    @FXML public Button butIsoCreate;

    // RadioButtons - filter StoreUnits
    @FXML public Button radioStatusAll;
    @FXML public Button radioStatusAllInner;
    @FXML public Button radioStatusAvailable;
    @FXML public Button radioStatusAvailableInner;
    @FXML public Button radioStatusPrepared;
    @FXML public Button radioStatusPreparedInner;
    private final RadioButtonsToggle radioButtonsToggle;

    // Internal state
    private final ApplicationState state;
    private final MainSrv mainSrv;
    private final MessageSrv msgSrv;

    private ObservableList<OperatingDayFX> operatingDays;
    private ObservableList<StorageUnitFX> storageUnits;
    private List<StorageUnitStatus> statusesFilter;

    // Listeners
    private final ChangeListener<AppSettings> settingsChangeListener; // settings changed listener (change OperationDays period filter)
    private final ChangeListener<List<OperatingDayFX>> operationDaysListener; // Operation Days table listener
    private final ChangeListener<OperatingDayFX> selectOperationDayListener; // Select row in OperationDays table
    private final ChangeListener<List<FileISO>> isoFilesChangeListener; // ISO_FILES in cache changed
    private final ChangeListener<StorageUnitFX> selectStorageUnitListener; // select row in StorageUnits table

    //endregion

    public MainCtl(ApplicationState state,
                   MainSrv mainSrv,
                   MessageSrv msgSrv
    ) {
        this.state = state;
        this.mainSrv = mainSrv;
        this.msgSrv = msgSrv;
        this.radioButtonsToggle = new RadioButtonsToggle();

        this.settingsChangeListener = (o, old, newVal) -> {
            // change operation days filter
            int filterDays = newVal.getFilterOpsDays();
            Platform.runLater( ()-> {
                    this.operDaysFilter.setText(String.valueOf(filterDays));
                    this.onReload(null);
            }  );
            // re-schedule data operationDays load
            int refreshIntervalMin = state.getSettings().getRefreshMin();
            this.mainSrv.scheduleReadInterval( refreshIntervalMin, filterDays);
        };
        this.isoFilesChangeListener = (o, old, newVal) -> {
            List<StorageUnitFX> withFileNames = this.getWithFileName(this.storageUnits, newVal);
            // filter and display a storage Units with fileNames
            Platform.runLater( ()-> this.filterAndDisplayStorageUnits(withFileNames, statusesFilter) );
        };
        this.operationDaysListener = (ob, oldVal, newVal) -> Platform.runLater(()-> displayOperatingDays(newVal));
        this.selectOperationDayListener = (o, old, newValue) -> {
            if (newValue != null) {
                // need to update storage units with isoFileName, stored in local file-cache
                final List<FileISO> fileCache = this.state.getIsoFiles();
                List<StorageUnitFX> withFileNames = this.getWithFileName(newValue.getStorageUnits(), fileCache);
                // filter and display a storage Units
                Platform.runLater(()-> this.filterAndDisplayStorageUnits(withFileNames, statusesFilter));
            }
        };
        this.selectStorageUnitListener = (o, oldVal, newVal) -> {
            butIsoLoad.setDisable(newVal == null || newVal.isDeleted());
            butIsoCreate.setDisable(newVal == null);
            butBurn.setDisable(newVal == null || Strings.isBlank(newVal.getIsoFileName())
                    || !Collections.unmodifiableList(Arrays.asList(StorageUnitStatus.READY_TO_RECORDING, StorageUnitStatus.RECORDED)).contains(newVal.getStorageUnitStatus()));
            butDelete.setDisable(newVal == null || Strings.isBlank(newVal.getIsoFileName()));
            butCheckSum.setDisable(newVal == null);
        };
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        logger.debug(location);

        // Setting table Operation Days
        this.operatingDays = FXCollections.emptyObservableList();
        this.tblOperatingDays.setItems(operatingDays);
        this.tblOperatingDays.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // set selection mode to only 1 row
        this.tblOperatingDays.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Таблица "Список операционных дней"
        this.operatingDay.setCellValueFactory(cell -> cell.getValue().operatingDayProperty());
        this.operatingDay.setComparator( this.sortDateStrings );
        this.typeSu.setCellValueFactory(cell -> cell.getValue().typeSuProperty());
        this.status.setCellValueFactory(cell -> cell.getValue().statusProperty());
        this.createdAt.setCellValueFactory(cell -> cell.getValue().createdAtProperty());
        this.createdAt.setComparator( this.sortDateStrings );

        // Таблица "Список единиц хранения"
        this.storageUnits = FXCollections.emptyObservableList();
        this.tblStorageUnits.setItems(storageUnits);
        this.tblStorageUnits.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // set selection mode to only 1 row
        this.tblStorageUnits.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.numberSu.setCellValueFactory(cell -> cell.getValue().numberSuProperty());
        this.creationDate.setCellValueFactory(cell -> cell.getValue().creationDateProperty());
        this.creationDate.setComparator( this.sortDateStrings );

        this.storageUnitStatus.setCellValueFactory(cell -> cell.getValue().storageUnitStatusProperty());
        this.savingDate.setCellValueFactory(cell -> cell.getValue().savingDateProperty());
        this.savingDate.setComparator( this.sortDateStrings );
        this.downloaded.setCellValueFactory(cell -> cell.getValue().downloadedProperty());
        this.present.setCellValueFactory(cell -> cell.getValue().presentProperty());

        // StoreUnitsStatus status filter (RadioButtons)
        this.radioButtonsToggle.add(radioStatusAll, radioStatusAllInner);
        this.radioButtonsToggle.add(radioStatusAvailable, radioStatusAvailableInner);
        this.radioButtonsToggle.add(radioStatusPrepared, radioStatusPreparedInner);
        this.radioButtonsToggle.setActive(radioStatusAll);

        // When user logged, binding listeners and unbinding otherwise
        this.state.userDetailsProperty().addListener(
            (o, old, newVal) -> {
                if (newVal.isLogged()) {
                    // bind listeners
                    this.state.operatingDaysProperty().addListener(operationDaysListener); // Operation Days table listener
                    this.tblOperatingDays.getSelectionModel().selectedItemProperty().addListener( selectOperationDayListener ); // OperationDays: when select row should refresh StorageUnits table
                    this.state.settingsProperty().addListener(settingsChangeListener); // Operation Days period's filter (settings changed)
                    this.state.fileNamesProperty().addListener(isoFilesChangeListener); // ISO_FILES in cache changed;
                    this.tblStorageUnits.getSelectionModel().selectedItemProperty().addListener( selectStorageUnitListener );
                    // set filter data
                    this.operDaysFilter.setText( String.valueOf(state.getSettings().getFilterOpsDays()) );
                    // schedule data operationDays load
                    int filterDays = state.getSettings().getFilterOpsDays();
                    int refreshIntervalMin = state.getSettings().getRefreshMin();
                    this.mainSrv.scheduleReadInterval( refreshIntervalMin, filterDays);
                } else {
                    this.state.operatingDaysProperty().removeListener(operationDaysListener); // Operation Days table listener
                    this.tblOperatingDays.getSelectionModel().selectedItemProperty().removeListener( selectOperationDayListener ); // OperationDays: when select row should refresh StorageUnits table
                    this.state.settingsProperty().removeListener(settingsChangeListener); // Operation Days period's filter (settings changed)
                    this.state.fileNamesProperty().removeListener(isoFilesChangeListener); // ISO_FILES in cache changed;
                    this.tblStorageUnits.getSelectionModel().selectedItemProperty().removeListener( selectStorageUnitListener );
                }
            }
        );

        // force the field to be numeric only
        this.operDaysFilter.textProperty().addListener((o, old, newValue) -> {
            if (!newValue.matches("\\d*")) {
                operDaysFilter.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        this.butFileCacheRefresh.setDisable( this.state.getRunMode() == RunMode.PROD );
        this.butFileCacheRefresh.setVisible( this.state.getRunMode() != RunMode.PROD );

    }

    /**
     * Choice filterStatusAll - Все
     * */
    @FXML public void onStatusAllChoice(ActionEvent ev) {
        logger.debug("");
        this.radioButtonsToggle.setActive(radioStatusAll);
        this.statusesFilter = null;
        this.filterAndDisplayStorageUnits(storageUnits, null);
    }

    /**
     * Choice filterStatusAvailable - Доступные для записи
     * */
    @FXML public void onStatusAvailableChoice(ActionEvent ev) {
        logger.debug("");
        this.radioButtonsToggle.setActive(radioStatusAvailable);
        this.statusesFilter = Collections.unmodifiableList(Arrays.asList(StorageUnitStatus.READY_TO_RECORDING, StorageUnitStatus.RECORDED));
        this.filterAndDisplayStorageUnits(storageUnits, this.statusesFilter);
    }

    /**
     * Choice filterStatusPrepared - Готовые для записи
     * */
    @FXML public void onStatusShowPrepChoice(ActionEvent ev) {
        logger.debug("");
        this.radioButtonsToggle.setActive(radioStatusPrepared);
        this.statusesFilter = Collections.singletonList(StorageUnitStatus.PREPARATION_FOR_RECORDING);
        this.filterAndDisplayStorageUnits(storageUnits, this.statusesFilter);
    }

    /**
     * Reload starts OperationDay's and StorageUnit's refresh
     * */
    @FXML public void onReload(ActionEvent ev) {

        logger.info("days: {}", operDaysFilter.getText());

        try {
            int days = Integer.parseUnsignedInt(operDaysFilter.getText());
            mainSrv.refreshDataAsync(days);
        } catch ( NumberFormatException ex) {
            logger.error("can't parse value to int: {}", operDaysFilter.getText(), ex);
        }
    }

    /**
     * Refresh filecache
     * visible only in DEV mode
     * */
    @FXML  public void onFileCacheRefresh(ActionEvent av) {
        mainSrv.readFileCacheAsync();
    }

    /**
     * Fire Reload when user ENTER key on Filter button
     * */
    @FXML public void onFilterEnter(KeyEvent ke) {
        if( ke.getCode() == KeyCode.ENTER ) {
            onReload(null);
        }
    }

    /**
     * Request backend (ABDD system) to create the ISO
     * */
    @FXML public void onIsoCreate(ActionEvent ev) {

        StorageUnitFX selected = tblStorageUnits.getSelectionModel().getSelectedItem();
        logger.info("SU: {}, objectId: {}", selected.getNumberSu(), selected.getObjectId());
        mainSrv.isoCreateAsync(selected);
    }

    /**
     * Load ISO file from ABDD server
     * */
    @FXML void onStartIsoLoad(ActionEvent ev) {

        StorageUnitFX selected = tblStorageUnits.getSelectionModel().getSelectedItem();
        logger.info("SU: {}, objectId: {}", selected.getNumberSu(), selected.getObjectId());
        mainSrv.loadISOAsync( selected.getObjectId() );
    }

    /**
     * Burn ISO disk
     * */
    @FXML public void onBurnIso(ActionEvent ev) {

        // choose a disk label
        String diskLabel = UtilsHelper.getDiskLabel();
        if (Strings.isBlank(diskLabel)) {
            return;
        }

        StorageUnitFX su = tblStorageUnits.getSelectionModel().selectedItemProperty().getValue();
        logger.info("Burn '{}' with label: '{}'", su.getNumberSu(), diskLabel);
        final int recorderIndex = 0;

        // check if the disk is ready
        RecorderInfo info = mainSrv.getRecorderInfo(recorderIndex);
        String msg = "Для продолжения записи вставьте новый диск в дисковод.";
        while (!info.isReady()) {
            if (!UtilsHelper.getConfirmation(msg)) {
                mainSrv.openTray(recorderIndex);
                return;
            }
            info = mainSrv.getRecorderInfo(recorderIndex);
            msg = "Диск поврежден. Запись невозможна. Вставьте в дисковод новый диск.";
        }

        mainSrv.burnISOAsync(su);
    }

    /**
     * Delete ISO files from cache
     * */
    @FXML public void onDeleteIso(ActionEvent ev) {
        StorageUnitFX su = tblStorageUnits.getSelectionModel().selectedItemProperty().getValue();
        logger.debug(su.getIsoFileName());
        mainSrv.deleteFileAsync(su.getIsoFileName());
    }

    /**
     * Checking the control sum of ISO files
     * */
    @FXML public void onCheckSum(ActionEvent ev) {

        String currentPath = this.state.getZipDir();
        if (Strings.isBlank(currentPath)) {
            this.msgSrv.news("Невозможно выполнить проверку контрольной суммы. DIR.zip не открыт");
            logger.warn("DIR.zip path not defined, exit");
            return;
        }
        logger.debug(currentPath);

        Path dirZip = Paths.get(currentPath, MyConst.DIR_ZIP);

        StorageUnitFX current = tblStorageUnits.getSelectionModel().selectedItemProperty().getValue();

        mainSrv.checkSumAsync(current.getObjectId(), dirZip);
    }

    //region PRIVATE

    /**
     * Update storeUnit fileName property if there is a filename in fileCache found
     * */
    List<StorageUnitFX> getWithFileName(List<StorageUnitFX> storageUnits, List<FileISO> fileCache) {

        return storageUnits.stream()
                .map(su -> {
                    StorageUnitFX updated = su;
                    if ( !Strings.isBlank(su.getObjectId()) ) {
                        String fullName = su.getObjectId() + ".iso";
                        final String fileName = fileCache.stream().anyMatch(f -> f.getFileName().equals(fullName)) ? fullName : "";
                        updated = new StorageUnitFX(
                                        su.getObjectId(),
                                        su.getOperatingDayId(),
                                        su.getNumberSu(),
                                        su.getCreationDate(),
                                        su.getDataSize(),
                                        su.getStorageDate(),
                                        su.getStorageUnitStatus(),
                                        su.getSavingDate(),
                                        fileName,
                                        su.isDeleted(),
                                        su.isPresent()
                                    );
                    }
                    return updated;
                })
                .collect(Collectors.toList());
    }

    /**
     * Refresh master OperatingDays table
     * Must be executed in Main Application Thread only!
     */
    private void displayOperatingDays(List<OperatingDayFX> operatingDays) {
        this.operatingDays = FXCollections.observableList(operatingDays);
        tblOperatingDays.setItems(this.operatingDays);
        // select first row
        if (operatingDays.size()>0) {
            tblOperatingDays.requestFocus();
            tblOperatingDays.getSelectionModel().select(0);
            tblOperatingDays.getFocusModel().focus(0);
        }
    }

    /**
     * Refresh slave storageUnits table
     * Must be executed in Main Application Thread only!
     */
    private void filterAndDisplayStorageUnits(List<StorageUnitFX> storageUnits, List<StorageUnitStatus> filter) {

        logger.debug("filter: {}", filter);
        this.storageUnits = FXCollections.observableList(storageUnits);
        // filter storageUnits if filter is not null
        ObservableList<StorageUnitFX> filtered =
                filter == null
                        ? this.storageUnits
                        : this.storageUnits.filtered(su -> filter.stream().anyMatch(f -> f.equals(su.getStorageUnitStatus())));
        // disable the storageUnits filter (radio-buttons) if no data available
        this.tblStorageUnits.setItems(filtered);
    }

    //endregion
}
