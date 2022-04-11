package ru.vez.iso.desktop.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;
import java.util.Objects;

/**
 * DataModel used in "Выбор Единицы хранения для записи на диск"
 * */
public class OperatingDayFX {

    private final ObjectProperty<LocalDate> operatingDay = new SimpleObjectProperty<>();
    private final ObjectProperty<ExType> typeSu = new SimpleObjectProperty<>();
    private final ObjectProperty<ExStatus> status = new SimpleObjectProperty<>();

    public OperatingDayFX(LocalDate operatingDay, ExType typeSu, ExStatus status) {
        this.operatingDay.set(operatingDay);
        this.typeSu.set(typeSu);
        this.status.set(status);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperatingDayFX dayFX = (OperatingDayFX) o;
        return getOperatingDay().equals(dayFX.getOperatingDay()) &&
                getTypeSu().equals(dayFX.getTypeSu()) &&
                getStatus().equals(dayFX.getStatus());
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
                '}';
    }
}
