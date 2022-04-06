package ru.vez.iso.desktop.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;

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
}
