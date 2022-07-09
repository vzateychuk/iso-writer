package ru.vez.iso.desktop.main.storeunits;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import org.apache.logging.log4j.util.Strings;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Data model StorageUnitFX used in MainForm
 * */
public class StorageUnitFX {

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String objectId;
    private final String operatingDayId;     // ID операционного дня
    private final StringProperty numberSu;
    private final ObjectProperty<LocalDate> creationDate;
    private final LongProperty dataSize;
    private final StringProperty storageDate;
    private final ObjectProperty<StorageUnitStatus> storageUnitStatus;
    private final ObjectProperty<LocalDate> savingDate;
    private final StringProperty isoFileName;
    private final BooleanProperty deleted;

    public StorageUnitFX(
            String objectId,
            String operatingDayId,
            String numberSu,
            LocalDate creationDate,
            Long dataSize,
            String storageDate,
            StorageUnitStatus storageUnitStatus,
            LocalDate savingDate,
            String isoFileName,
            boolean deleted) {
        this.objectId = objectId;
        this.operatingDayId = operatingDayId;
        this.numberSu = new SimpleStringProperty(numberSu);
        this.creationDate = new SimpleObjectProperty<>(creationDate);
        this.dataSize = new SimpleLongProperty(dataSize);
        this.storageDate = new SimpleStringProperty(storageDate);
        this.storageUnitStatus = new SimpleObjectProperty<>(storageUnitStatus);
        this.savingDate = new SimpleObjectProperty<>(savingDate);
        this.isoFileName = new SimpleStringProperty(isoFileName);
        this.deleted = new SimpleBooleanProperty(deleted);
    }

    public String getObjectId() {
        return objectId != null ? objectId : "";
    }

    public String getOperatingDayId() {
        return operatingDayId != null ? operatingDayId : "";
    }

    public String getNumberSu() {
        return numberSu.get();
    }
    public StringProperty numberSuProperty() {
        return numberSu;
    }

    public LocalDate getCreationDate() {
        return creationDate.get();
    }
    public StringProperty creationDateProperty() {
        String result = creationDate.get() != null ? creationDate.get().format(fmt) : "";
        return new SimpleStringProperty(result);
    }

    public Long getDataSize() {
        return dataSize.get();
    }
    public ObservableValue<Long> dataSizeProperty() {
        return dataSize.asObject();
    }

    public String getStorageDate() {
        return storageDate.get();
    }
    public StringProperty storageDateProperty() {
        return storageDate;
    }

    public StorageUnitStatus getStorageUnitStatus() {
        return storageUnitStatus.get();
    }
    public StringProperty storageUnitStatusProperty() {
        return new SimpleStringProperty(storageUnitStatus.get().getTitle());
    }

    public LocalDate getSavingDate() {
        return savingDate.get();
    }
    public StringProperty savingDateProperty() {
        String result = savingDate.get() != null ? savingDate.get().format(fmt) : "";
        return new SimpleStringProperty(result);
    }

    public String getIsoFileName() {
        return isoFileName.get();
    }
    public StringProperty isoFileNameProperty() {
        return isoFileName;
    }

    public StringProperty downloadedProperty() {
        String dowloaded = Strings.isBlank(isoFileName.get()) ? "Нет" : "Да";
        return new SimpleStringProperty(dowloaded);

    }
    // TODO implementation the formed property is waiting backend implementation
    public StringProperty formedProperty() {
        return new SimpleStringProperty("Нет");
    }

    public boolean isDeleted() {
        return deleted.get();
    }
    public StringProperty deletedProperty() {
        return new SimpleStringProperty(deleted.get() ? "Да" : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StorageUnitFX that = (StorageUnitFX) o;
        return getObjectId().equals(that.getObjectId()) &&
                getOperatingDayId().equals(that.getOperatingDayId()) &&
                getNumberSu().equals(that.getNumberSu()) &&
                getCreationDate().equals(that.getCreationDate()) &&
                Objects.equals(getDataSize(), that.getDataSize()) &&
                Objects.equals(getStorageDate(), that.getStorageDate()) &&
                Objects.equals(getStorageUnitStatus(), that.getStorageUnitStatus()) &&
                Objects.equals(getSavingDate(), that.getSavingDate()) &&
                Objects.equals(isoFileNameProperty(), that.isoFileNameProperty());
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectId, numberSu, creationDate, dataSize, storageDate, storageUnitStatus, savingDate, isoFileName);
    }

    @Override
    public String toString() {
        return "StorageUnit{" +
                "objectId='" + objectId + '\'' +
                ", operatingDayId='" + operatingDayId + '\'' +
                ", numberSu=" + numberSu +
                ", creationDate=" + creationDate +
                ", dataSize=" + dataSize +
                ", storageDate=" + storageDate +
                ", storageUnitStatus=" + storageUnitStatus +
                ", savingDate=" + savingDate +
                '}';
    }
}
