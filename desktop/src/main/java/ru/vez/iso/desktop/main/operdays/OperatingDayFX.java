package ru.vez.iso.desktop.main.operdays;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private final StringProperty typeSu = new SimpleStringProperty();   // Тип носителя, строковое значение
    private final ObjectProperty<OperDayStatus> status = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> createdAt = new SimpleObjectProperty<>();
    private final StringProperty numberSu = new SimpleStringProperty();

    public OperatingDayFX(String objectId,
                          LocalDate operatingDay,
                          String typeSu,
                          OperDayStatus status,
                          LocalDate createdAt,
                          String numberSu) {
        this.objectId = objectId;
        this.operatingDay.set(operatingDay);
        this.typeSu.set(typeSu);
        this.status.set(status);
        this.createdAt.set(createdAt);
        this.numberSu.set(numberSu);
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

    public String getTypeSu() {
        return typeSu.get();
    }
    public StringProperty typeSuProperty() {
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

    public String getNumberSu() {
        return numberSu.get();
    }
    public StringProperty numberSuProperty() {
        return numberSu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperatingDayFX that = (OperatingDayFX) o;
        return getOperatingDay().equals(that.getOperatingDay()) &&
                getTypeSu().equals(that.getTypeSu()) &&
                getStatus().equals(that.getStatus()) &&
                getCreatedAt().equals(that.getCreatedAt()) &&
                getNumberSu().equals(that.getNumberSu());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOperatingDay(), getTypeSu(), getStatus());
    }

    @Override
    public String toString() {
        return "OperatingDay{" +
                "operationDate=" + this.getOperatingDay().format(formatter) +
                ", typeSU=" + this.getTypeSu() +
                ", status=" + this.getStatus() +
                ", created=" + this.getCreatedAt().format(formatter) +
                ", numberSU=" + this.getNumberSu() +
                '}';
    }
}
