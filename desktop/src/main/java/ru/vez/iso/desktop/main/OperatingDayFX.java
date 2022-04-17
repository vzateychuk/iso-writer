package ru.vez.iso.desktop.main;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * DataModel "Операционный день"
 * used "Список операционных дней"
 * in View main "Выбор Единицы хранения для записи на диск"
 * */
public class OperatingDayFX {

    private final String objectId;
    private final ObjectProperty<LocalDate> operatingDay = new SimpleObjectProperty<>();
    private final ObjectProperty<TypeSu> typeSu = new SimpleObjectProperty<>();
    private final ObjectProperty<OpsDayStatus> status = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> createdAt = new SimpleObjectProperty<>();
    private final SimpleBooleanProperty edited = new SimpleBooleanProperty();
    private List<StorageUnitFX> storageUnits = Collections.emptyList();

    public OperatingDayFX(String objectId,
                          LocalDate operatingDay,
                          TypeSu typeSu,
                          OpsDayStatus status,
                          LocalDate createdAt,
                          boolean edited) {
        this.objectId = objectId;
        this.operatingDay.set(operatingDay);
        this.typeSu.set(typeSu);
        this.status.set(status);
        this.createdAt.set(createdAt);
        this.edited.set(edited);
    }

    public String getObjectId() {
        return objectId;
    }

    public LocalDate getOperatingDay() {
        return operatingDay.get();
    }
    public ObjectProperty<LocalDate> operatingDayProperty() {
        return operatingDay;
    }

    public TypeSu getTypeSu() {
        return typeSu.get();
    }
    public ObjectProperty<TypeSu> typeSuProperty() {
        return typeSu;
    }

    public OpsDayStatus getStatus() {
        return status.get();
    }
    public ObjectProperty<OpsDayStatus> statusProperty() {
        return status;
    }

    public LocalDate getCreatedAt() {
        return createdAt.get();
    }
    public ObjectProperty<LocalDate> createdAtProperty() {
        return createdAt;
    }

    public boolean isEdited() {
        return edited.get();
    }
    public SimpleBooleanProperty editedProperty() {
        return edited;
    }

    public List<StorageUnitFX> getStorageUnits() {
        return storageUnits;
    }
    public void setStorageUnits(List<StorageUnitFX> storageUnits) {
        this.storageUnits = storageUnits == null ? Collections.emptyList() : storageUnits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperatingDayFX dayFX = (OperatingDayFX) o;
        return getOperatingDay().equals(dayFX.getOperatingDay()) &&
                getTypeSu().equals(dayFX.getTypeSu()) &&
                getStatus().equals(dayFX.getStatus()) &&
                getCreatedAt().equals(dayFX.getCreatedAt()) &&
                isEdited() == dayFX.isEdited();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOperatingDay(), getTypeSu(), getStatus());
    }

    @Override
    public String toString() {
        return "OperatingDay{" +
                "date=" + getOperatingDay() +
                ", typeSU=" + getTypeSu() +
                ", status=" + getStatus() +
                ", created=" + getCreatedAt() +
                ", isEdited=" + isEdited() +
                '}';
    }
}
