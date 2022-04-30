package ru.vez.iso.desktop.shared;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * DataModel for Disks View
 * */
public class IsoFileFX {

    private final StringProperty fileName;
    private final StringProperty numberSu;

    public IsoFileFX(String fileName, String numberSu) {
        this.fileName = new SimpleStringProperty(fileName);
        this.numberSu = new SimpleStringProperty(numberSu);
    }

    public String getFileName() {
        return fileName.get();
    }
    public StringProperty fileNameProperty() {
        return fileName;
    }

    public String getNumberSu() {
        return numberSu.get();
    }
    public StringProperty numberSuProperty() {
        return numberSu;
    }

    @Override
    public String toString() {
        return "IsoFileFX{" +
                "fileName=" + fileName +
                ", numberSu=" + numberSu +
                '}';
    }
}
