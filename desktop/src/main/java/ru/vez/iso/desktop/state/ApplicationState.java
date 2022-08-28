package ru.vez.iso.desktop.state;

import javafx.beans.property.*;
import ru.vez.iso.desktop.docs.DocumentFX;
import ru.vez.iso.desktop.docs.reestr.Reestr;
import ru.vez.iso.desktop.main.operdays.OperatingDayFX;
import ru.vez.iso.desktop.shared.AppSettings;
import ru.vez.iso.desktop.shared.FileISO;
import ru.vez.iso.desktop.shared.UserDetails;

import java.util.Collections;
import java.util.List;

public class ApplicationState {

    private final ObjectProperty<RunMode> runMode;              // Режим запуска приложения: Enum RunMode
    private final ObjectProperty<AppSettings> settings;         // Настройки приложения
    private final StringProperty zipDir;                        // Путь к файлу DIR.zip открытый пользователем в форме "Документы"
    private final ObjectProperty<UserDetails> userDetails;      // Режим запуска приложения: Enum RunMode
    private final ObjectProperty<List<OperatingDayFX>> operatingDays;   // Операционные дни
    private final ObjectProperty<List<FileISO>>  fileNames;     // Список ISO файлов, загруженных из backend и хранящихся в cache
    private final ObjectProperty<List<DocumentFX>> documentFXs; // Список документов загруженных из REESTR файла
    private final ObjectProperty<Reestr>  reestr;               // Загруженный реестр документов (из DirZip.zip)
    private final BooleanProperty burning;                      // Флаг обозначающий что идет процесс записи на диск

    /**
     * Создает ApplicationState с пустыми значениями
     * */
    public ApplicationState(
    ) {
        this.runMode = new SimpleObjectProperty<>(RunMode.NOOP);
        this.settings = new SimpleObjectProperty<>(AppSettings.builder().build());
        this.zipDir = new SimpleStringProperty("");
        this.userDetails = new SimpleObjectProperty<>(UserDetails.NOT_SIGNED_USER);
        this.operatingDays = new SimpleObjectProperty<>(Collections.emptyList());
        this.fileNames = new SimpleObjectProperty<>(Collections.emptyList());
        this.documentFXs = new SimpleObjectProperty<>(Collections.emptyList());
        this.reestr = new SimpleObjectProperty<>(new Reestr());
        this.burning = new SimpleBooleanProperty(false);
    }

    public RunMode getRunMode() {
        return runMode.get();
    }
    public ObjectProperty<RunMode> runModeProperty() {
        return runMode;
    }
    public void setRunMode(RunMode runMode) {
        this.runMode.set(runMode);
    }

    public String getZipDir() {
        return zipDir.get();
    }
    public StringProperty zipDirProperty() {
        return zipDir;
    }
    public void setZipDir(String zipDir) {
        this.zipDir.set(zipDir);
    }

    public UserDetails getUserDetails() {
        return userDetails.get();
    }
    public ObjectProperty<UserDetails> userDetailsProperty() {
        return userDetails;
    }
    public void setUserDetails(UserDetails userDetails) {
        this.userDetails.set(userDetails);
    }

    public List<OperatingDayFX> getOperatingDays() {
        return operatingDays.get();
    }
    public ObjectProperty<List<OperatingDayFX>> operatingDaysProperty() {
        return operatingDays;
    }
    public void setOperatingDays(List<OperatingDayFX> operatingDays) {
        this.operatingDays.set(operatingDays);
    }

    public List<DocumentFX> getDocumentFXs() {
        return documentFXs.get();
    }
    public ObjectProperty<List<DocumentFX>> documentFXsProperty() {
        return documentFXs;
    }
    public void setDocumentFXs(List<DocumentFX> documentFXs) {
        this.documentFXs.set(documentFXs);
    }

    public List<FileISO> getIsoFiles() {
        return fileNames.get();
    }
    public ObjectProperty<List<FileISO>> fileNamesProperty() {
        return fileNames;
    }
    public void setFileNames(List<FileISO> isoList) {
        this.fileNames.set(isoList);
    }

    public AppSettings getSettings() {
        return settings.get();
    }
    public ObjectProperty<AppSettings> settingsProperty() {
        return settings;
    }
    public void setSettings(AppSettings settings) {
        this.settings.set(settings);
    }

    public Reestr getReestr() {
        return reestr.get();
    }
    public ObjectProperty<Reestr> reestrProperty() {
        return reestr;
    }
    public void setReestr(Reestr reestr) {
        this.reestr.set(reestr);
    }

    public boolean isBurning() {
        return burning.get();
    }
    public BooleanProperty burningProperty() {
        return burning;
    }
    public void setBurning(boolean burning) {
        this.burning.set(burning);
    }
}
