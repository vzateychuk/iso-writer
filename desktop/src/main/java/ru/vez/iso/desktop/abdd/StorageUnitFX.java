package ru.vez.iso.desktop.abdd;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;

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
    private final String operatingDayId;     // ID операционного дня
    private final StringProperty numberSu;   // Отображаемый бизнес-идентификатор
    private final ObjectProperty<LocalDate> creationDate;
    private final IntegerProperty dataSize;
    private final ObjectProperty<LocalDate> storageDate;
    private final ObjectProperty<StorageUnitStatus> storageUnitStatus;
    private final ObjectProperty<LocalDate> savingDate;
    private final StringProperty isoFileName;

    public StorageUnitFX(
            String objectId,
            String operatingDayId,
            String numberSu,
            LocalDate creationDate,
            Integer dataSize,
            LocalDate storageDate,
            StorageUnitStatus storageUnitStatus,
            LocalDate savingDate,
            String isoFileName
    ) {
        this.objectId = objectId;
        this.operatingDayId = operatingDayId;
        this.numberSu = new SimpleStringProperty(numberSu);
        this.creationDate = new SimpleObjectProperty<>(creationDate);
        this.dataSize = new SimpleIntegerProperty(dataSize);
        this.storageDate = new SimpleObjectProperty<>(storageDate);
        this.storageUnitStatus = new SimpleObjectProperty<>(storageUnitStatus);
        this.savingDate = new SimpleObjectProperty<>(savingDate);
        this.isoFileName = new SimpleStringProperty(isoFileName);
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
        return new SimpleStringProperty(creationDate.get().format(fmt));
    }

    public Integer getDataSize() {
        return dataSize.get();
    }
    public ObservableValue<Integer> dataSizeProperty() {
        return dataSize.asObject();
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

    public String getIsoFileName() {
        return isoFileName.get();
    }
    public StringProperty isoFileNameProperty() {
        return isoFileName;
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
