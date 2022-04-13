package ru.vez.iso.desktop.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;
import java.util.Objects;

/**
 * DataModel "Операционный день"
 * used "Список операционных дней"
 * in View main "Выбор Единицы хранения для записи на диск"
 * */
public class OperatingDayFX {

    private final ObjectProperty<String> objectId = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> operatingDay = new SimpleObjectProperty<>();
    private final ObjectProperty<ExType> typeSu = new SimpleObjectProperty<>();
    private final ObjectProperty<ExStatus> status = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> createdAt = new SimpleObjectProperty<>();
    private final SimpleBooleanProperty edited = new SimpleBooleanProperty();

    public OperatingDayFX(String objectId,
                          LocalDate operatingDay,
                          ExType typeSu,
                          ExStatus status,
                          LocalDate createdAt,
                          boolean edited) {
        this.objectId.set(objectId);
        this.operatingDay.set(operatingDay);
        this.typeSu.set(typeSu);
        this.status.set(status);
        this.createdAt.set(createdAt);
        this.edited.set(edited);
    }

    public String getObjectId() {
        return objectId.get();
    }
    public ObjectProperty<String> objectIdProperty() {
        return objectId;
    }

    public LocalDate getOperatingDay() {
        return operatingDay.get();
    }
    public ObjectProperty<LocalDate> operatingDayProperty() {
        return operatingDay;
    }

    public ExType getTypeSu() {
        return typeSu.get();
    }
    public ObjectProperty<ExType> typeSuProperty() {
        return typeSu;
    }

    public ExStatus getStatus() {
        return status.get();
    }
    public ObjectProperty<ExStatus> statusProperty() {
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
