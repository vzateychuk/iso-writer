package ru.vez.iso.desktop.disks;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * DataModel for Disks View */
public class IsoFileFX {

    private final StringProperty docNumber;
    private final StringProperty fileName;

    public IsoFileFX(String docNumber, String fileName) {
        this.docNumber = new SimpleStringProperty(docNumber);
        this.fileName = new SimpleStringProperty(fileName);
    }

    public String getDocNumber() {
        return docNumber.get();
    }
    public StringProperty docNumberProperty() {
        return docNumber;
    }

    public String getFileName() {
        return fileName.get();
    }
    public StringProperty fileNameProperty() {
        return fileName;
    }

}
