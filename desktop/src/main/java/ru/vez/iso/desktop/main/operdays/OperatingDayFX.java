package ru.vez.iso.desktop.main.operdays;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import ru.vez.iso.desktop.main.StorageUnitFX;
import ru.vez.iso.desktop.main.TypeSu;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * DataModel "Операционный день"
 * used "Список операционных дней"
 * in View main "Выбор Единицы хранения для записи на диск"
 * */
public class OperatingDayFX {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String objectId;
    private final ObjectProperty<LocalDate> operatingDay = new SimpleObjectProperty<>();
    private final ObjectProperty<TypeSu> typeSu = new SimpleObjectProperty<>();
    private final ObjectProperty<OperDayStatus> status = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> createdAt = new SimpleObjectProperty<>();
    private List<StorageUnitFX> storageUnits = Collections.emptyList();

    public OperatingDayFX(String objectId,
                          LocalDate operatingDay,
                          TypeSu typeSu,
                          OperDayStatus status,
                          LocalDate createdAt) {
        this.objectId = objectId;
        this.operatingDay.set(operatingDay);
        this.typeSu.set(typeSu);
        this.status.set(status);
        this.createdAt.set(createdAt);
    }

    public String getObjectId() {
        return objectId;
    }

    public LocalDate getOperatingDay() {
        return operatingDay.get();
    }
    public StringProperty operatingDayProperty() {
        return new SimpleStringProperty(operatingDay.get().format(formatter));
    }

    public TypeSu getTypeSu() {
        return typeSu.get();
    }
    public ObjectProperty<TypeSu> typeSuProperty() {
        return typeSu;
    }

    public OperDayStatus getStatus() {
        return status.get();
    }
    public StringProperty statusProperty() {
        return new SimpleStringProperty(status.get().getTitle());
    }

    public LocalDate getCreatedAt() {
        return createdAt.get();
    }
    public StringProperty createdAtProperty() {
        return new SimpleStringProperty(createdAt.get().format(formatter));
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
                getCreatedAt().equals(dayFX.getCreatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOperatingDay(), getTypeSu(), getStatus());
    }

    @Override
    public String toString() {
        return "OperatingDay{" +
                "date=" + getOperatingDay().format(formatter) +
                ", typeSU=" + getTypeSu() +
                ", status=" + getStatus() +
                ", created=" + getCreatedAt().format(formatter) +
                '}';
    }
}
