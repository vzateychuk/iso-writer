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

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Controller for: "Выбор Единицы хранения для записи на диск"
 * */
@Log
public class MainCtl implements Initializable {

    //region Properties

    private static final Logger logger = LogManager.getLogger();
    private static final List<StorageUnitStatus> AVAILABLE_FOR_BURN = Collections.unmodifiableList(
            Arrays.asList(
                    StorageUnitStatus.READY_TO_RECORDING,
                    StorageUnitStatus.RECORDED
            )
    );

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
    @FXML private TableColumn<OperatingDayFX, String> numberSU;

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
    @FXML private Button butBurn;
    @FXML private Button butDelete;
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

/*
    private ObservableList<OperatingDayFX> operatingDays;
    private ObservableList<StorageUnitFX> storageUnits;
*/
    private List<StorageUnitStatus> statusesFilter;

    // Listeners
    private final ChangeListener<AppSettings> settingsChangeListener; // settings changed listener (change OperationDays period filter)
    private final ChangeListener<List<OperatingDayFX>> operationDaysListener; // Operation Days table listener
    private final ChangeListener<OperatingDayFX> selectOperationDayListener; // Select row in OperationDays table
    private final ChangeListener<List<FileISO>> isoFilesChangeListener; // ISO_FILES in cache changed
    private final ChangeListener<StorageUnitFX> selectStorageUnitListener; // select row in StorageUnits table
    private final ChangeListener<Boolean> burningListener; // if it's burning

    Predicate<StorageUnitFX> disableIsoLoad =
            su -> su == null || !Strings.isBlank(su.getIsoFileName())
                    || !AVAILABLE_FOR_BURN.contains( su.getStorageUnitStatus() );

    Predicate<StorageUnitFX> disableBurn =
            su -> su == null || Strings.isBlank(su.getIsoFileName())
                    || !AVAILABLE_FOR_BURN.contains( su.getStorageUnitStatus() );

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
            // filter and display a storage Units with fileNames
            // Platform.runLater( ()->this.filterAndDisplayStorageUnits(this.storageUnits, statusesFilter) );
        };
        this.operationDaysListener = (ob, old, opDaysList) -> Platform.runLater(()-> displayOperatingDays(opDaysList));
        this.selectOperationDayListener = (o, old, selectedOpDay) -> {
            if (selectedOpDay != null) {
                // filter and display a storage Units
                Platform.runLater(() -> this.filterAndDisplayStorageUnits(selectedOpDay.getObjectId(), statusesFilter));
            }
        };
        this.selectStorageUnitListener = (o, old, selectedSU) -> {
                    butIsoLoad.setDisable( disableIsoLoad.test(selectedSU) );
                    butIsoCreate.setDisable(selectedSU == null || selectedSU.isPresent());
                    butBurn.setDisable( disableBurn.test(selectedSU) || this.state.isBurning() );
                    butDelete.setDisable(selectedSU == null || Strings.isBlank(selectedSU.getIsoFileName()));
                };

        // сделать доступной/недоступной кнопку Burn если мы уже прожигаем или
        this.burningListener = (o, old, isBurning) -> Platform.runLater( () -> {
            butBurn.setDisable( isBurning || disableBurn.test(tblStorageUnits.getSelectionModel().getSelectedItem()) );
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        logger.debug(location);

        // Setting table Operation Days
        // this.operatingDays = FXCollections.emptyObservableList();
        this.tblOperatingDays.setItems(FXCollections.emptyObservableList());
        this.tblOperatingDays.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // set selection mode to only 1 row
        this.tblOperatingDays.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Таблица "Список операционных дней"
        this.operatingDay.setCellValueFactory(cell -> cell.getValue().operatingDayProperty());
        this.operatingDay.setComparator( this.sortDateStrings );
        this.typeSu.setCellValueFactory(cell -> cell.getValue().typeSuProperty());
        this.status.setCellValueFactory(cell -> cell.getValue().statusProperty());
        this.numberSU.setCellValueFactory(cell -> cell.getValue().numberSuProperty());

        // Таблица "Список единиц хранения"
        // this.storageUnits = FXCollections.emptyObservableList();
        this.tblStorageUnits.setItems(FXCollections.emptyObservableList());
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
            (o, old, newUserDetails) -> {
                if ( newUserDetails.isLogged() ) {
                    // bind listeners
                    this.state.operatingDaysProperty().addListener(operationDaysListener); // Operation Days table listener
                    this.tblOperatingDays.getSelectionModel().selectedItemProperty().addListener( selectOperationDayListener ); // OperationDays: when select row should refresh StorageUnits table
                    this.state.settingsProperty().addListener(settingsChangeListener); // Operation Days period's filter (settings changed)
                    this.state.fileNamesProperty().addListener(isoFilesChangeListener); // ISO_FILES in cache changed;
                    this.tblStorageUnits.getSelectionModel().selectedItemProperty().addListener( selectStorageUnitListener );
                    this.state.burningProperty().addListener( this.burningListener );
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
                    this.state.burningProperty().removeListener( this.burningListener );
                }
            }
        );

        // force the field to be numeric only
        this.operDaysFilter.textProperty().addListener((o, old, newValue) -> {
            if (!newValue.matches("\\d*")) {
                operDaysFilter.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

    }

    /**
     * Choice filterStatusAll - Все
     * */
    @FXML public void onStatusAllChoice(ActionEvent ev) {

        logger.debug("");
        this.radioButtonsToggle.setActive(radioStatusAll);
        this.statusesFilter = null;
        String opDayId = tblOperatingDays.getSelectionModel().getSelectedItem().getObjectId();
        this.filterAndDisplayStorageUnits(opDayId, null);
    }

    /**
     * Choice filterStatusAvailable - Доступные для записи
     * */
    @FXML public void onStatusAvailableChoice(ActionEvent ev) {
        logger.debug("storageUnits filter: READY_TO_RECORDING + RECORDED");
        this.radioButtonsToggle.setActive(radioStatusAvailable);
        this.statusesFilter = Collections.unmodifiableList(
                Arrays.asList(
                        StorageUnitStatus.READY_TO_RECORDING,
                        StorageUnitStatus.RECORDED
                )
        );
        String opDayId = tblOperatingDays.getSelectionModel().getSelectedItem().getObjectId();
        this.filterAndDisplayStorageUnits(opDayId, this.statusesFilter);
    }

    /**
     * Choice filterStatusPrepared - Готовые для записи
     * */
    @FXML public void onStatusShowPrepChoice(ActionEvent ev) {
        logger.debug("storageUnits filter: PREPARATION_FOR_RECORDING");
        this.radioButtonsToggle.setActive(radioStatusPrepared);
        this.statusesFilter = Collections.singletonList(StorageUnitStatus.PREPARATION_FOR_RECORDING);
        String opDayId = tblOperatingDays.getSelectionModel().getSelectedItem().getObjectId();
        this.filterAndDisplayStorageUnits(opDayId, this.statusesFilter);
    }

    /**
     * Reload starts OperationDay's and StorageUnit's refresh
     * */
    @FXML public void onReload(ActionEvent ev) {

        logger.debug("days: {}", operDaysFilter.getText());

        int days = this.state.getSettings().getFilterOpsDays();
        try {
            days = Integer.parseUnsignedInt(operDaysFilter.getText());
            if (days == 0) {
                throw new IllegalArgumentException(operDaysFilter.getText());
            }
        } catch ( IllegalArgumentException ex) {
            logger.error("Bad value: {}", operDaysFilter.getText(), ex);
        }

        String opDayId = tblOperatingDays.getSelectionModel().getSelectedItem().getObjectId();
        mainSrv.refreshDataAsync(days,
                ()->Platform.runLater(
                        ()-> this.filterAndDisplayStorageUnits(opDayId, this.statusesFilter)
                )
        );
    }

    /**
     * Fire Reload when user ENTER key on Filter button
     * */
    @FXML public void onFilterEnter(KeyEvent ke) {
        logger.debug("");
        if( ke.getCode() == KeyCode.ENTER ) {
            onReload(null);
        }
    }

    /**
     * Request backend (ABDD system) to create the ISO
     * */
    @FXML public void onIsoCreate(ActionEvent ev) {

        logger.debug("");
        StorageUnitFX selected = tblStorageUnits.getSelectionModel().getSelectedItem();
        logger.info("SU: {}, objectId: {}", selected.getNumberSu(), selected.getObjectId());
        mainSrv.isoCreateAsync(selected);
    }

    /**
     * Load ISO file from ABDD server
     * */
    @FXML void onStartIsoLoad(ActionEvent ev) {

        logger.debug("");
        StorageUnitFX selected = tblStorageUnits.getSelectionModel().getSelectedItem();
        logger.info("SU: {}, objectId: {}", selected.getNumberSu(), selected.getObjectId());
        String opDayId = tblOperatingDays.getSelectionModel().getSelectedItem().getObjectId();
        mainSrv.loadISOAsync( selected,
                su -> Platform.runLater( ()->this.filterAndDisplayStorageUnits(opDayId,this.statusesFilter) )
        );
    }

    /**
     * Burn ISO disk
     * */
    @FXML public void onBurnIso(ActionEvent event) {

        logger.debug("");
        // check if previous burning session hasn't completed
        if (this.state.isBurning()) {
            logger.warn("not expect call until burning complete");
            this.msgSrv.news("Запись диска не окончена, подождите");
            return;
        }

        // choose a disk label
        String labelMainOrReserve = UtilsHelper.getDiskLabel();
        if (Strings.isBlank(labelMainOrReserve)) {
            this.msgSrv.news("Запись диска отменена");
            return;
        }

        StorageUnitFX su = tblStorageUnits.getSelectionModel().selectedItemProperty().getValue();
        logger.info("Burn '{}' with label: '{}'", su.getNumberSu(), labelMainOrReserve);
        final int recorderIndex = 0;

        // check if the disk is ready
        RecorderInfo info = mainSrv.getRecorderInfo(recorderIndex);
        logger.debug(info.toString());
        String msg = "Для продолжения записи вставьте новый диск в дисковод.";
        while (!info.isReady()) {
            if (!UtilsHelper.getConfirmation(msg)) {
                mainSrv.openTray(recorderIndex);
                return;
            }
            info = mainSrv.getRecorderInfo(recorderIndex);
            msg = "Диск поврежден. Запись невозможна. Вставьте в дисковод новый диск.";
        }

        mainSrv.burnISOAsync(su, su.getNumberSu() + "_" + labelMainOrReserve + "_носитель");
    }

    /**
     * Delete ISO files from cache
     * */
    @FXML public void onDeleteIso(ActionEvent ev) {
        logger.debug("");
        StorageUnitFX su = tblStorageUnits.getSelectionModel().selectedItemProperty().getValue();
        logger.debug(su.getIsoFileName());
        mainSrv.deleteFileAsync(su.getIsoFileName());
    }

    //region PRIVATE

    /**
     * Refresh master OperatingDays table
     * Must be executed in Main Application Thread only!
     */
    private void displayOperatingDays(List<OperatingDayFX> operatingDays) {
        // this.operatingDays = FXCollections.observableList(operatingDays);
        tblOperatingDays.setItems( FXCollections.observableList(operatingDays) );
        // select first row
/*
        if (!operatingDays.isEmpty()) {
            tblOperatingDays.requestFocus();
            tblOperatingDays.getSelectionModel().select(0);
            tblOperatingDays.getFocusModel().focus(0);
        }
*/
    }

    /**
     * Refresh slave storageUnits table
     * Must be executed in Main Application Thread only!
     */
    private void filterAndDisplayStorageUnits(String opDayId, List<StorageUnitStatus> statuses) {

        logger.debug("filter: {}", statuses);
        List<StorageUnitStatus> statusFilter = statuses == null
                ? Arrays.stream(StorageUnitStatus.values()).collect(Collectors.toList())
                : statuses;

        // filter storageUnits if filter is not null
        ObservableList<StorageUnitFX> filtered = FXCollections.observableList(
                this.state.getStorageUnits().stream()
                .filter( su ->
                        opDayId.equals(su.getOperatingDayId()) &&
                        statusFilter.stream().anyMatch(
                                f -> f.equals(su.getStorageUnitStatus())
                        )
                )
                .collect(Collectors.toList() ) );

        this.tblStorageUnits.setItems(filtered);
    }

    //endregion
}
