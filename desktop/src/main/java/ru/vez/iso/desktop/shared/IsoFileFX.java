package ru.vez.iso.desktop.shared;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * DataModel for Disks View
 * */
public class IsoFileFX {

    private final StringProperty fileName;

    public IsoFileFX(String fileName) {
        this.fileName = new SimpleStringProperty(fileName);
    }

    public String getFileName() {
        return fileName.get();
    }
    public StringProperty fileNameProperty() {
        return fileName;
    }

    @Override
    public String toString() {
        return "IsoFileFX{" +
                "fileName=" + fileName +
                '}';
    }
}
