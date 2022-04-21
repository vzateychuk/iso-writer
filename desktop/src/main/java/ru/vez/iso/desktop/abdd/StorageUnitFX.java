package ru.vez.iso.desktop.abdd;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Data model StorageUnitFX
 * used "Список операционных дней"
 * */
public class StorageUnitFX {

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String objectId;
    private final String operatingDayId;
    private final SimpleStringProperty numberSu = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> creationDate = new SimpleObjectProperty<>();
    private final ObjectProperty<Integer> dataSize = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> storageDate = new SimpleObjectProperty<>();
    private final ObjectProperty<StorageUnitStatus> storageUnitStatus = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> savingDate = new SimpleObjectProperty<>();

    public StorageUnitFX(
            String objectId,
            String operatingDayId,
            String numberSu,
            LocalDate creationDate,
            Integer dataSize,
            LocalDate storageDate,
            StorageUnitStatus storageUnitStatus,
            LocalDate savingDate
    ) {
        this.objectId = objectId;
        this.operatingDayId = operatingDayId;
        this.numberSu.set(numberSu);
        this.creationDate.set(creationDate);
        this.dataSize.set(dataSize);
        this.storageDate.set(storageDate);
        this.storageUnitStatus.set(storageUnitStatus);
        this.savingDate.set(savingDate);
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
    public SimpleStringProperty numberSuProperty() {
        return numberSu;
    }

    public LocalDate getCreationDate() {
        return creationDate.get();
    }
    public StringProperty creationDateProperty() {
        return new SimpleStringProperty(creationDate.get().format(fmt));
    }

    public Integer getDataSize() {
        return dataSize.get();
    }
    public ObjectProperty<Integer> dataSizeProperty() {
        return dataSize;
    }

    public LocalDate getStorageDate() {
        return storageDate.get();
    }
    public StringProperty storageDateProperty() {
        return new SimpleStringProperty(storageDate.get().format(fmt));
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
        return new SimpleStringProperty(savingDate.get().format(fmt));
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
                Objects.equals(getSavingDate(), that.getSavingDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectId, numberSu, creationDate, dataSize, storageDate, storageUnitStatus, savingDate);
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
