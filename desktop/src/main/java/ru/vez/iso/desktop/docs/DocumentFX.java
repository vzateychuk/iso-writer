package ru.vez.iso.desktop.docs;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * DataModel "Документ Единицы хранения"
 * */
public class DocumentFX {

    private final DateTimeFormatter displayFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final String objectId;              // Уникальный идентификатор из вебсервиса
    private final StringProperty docNumber;     // Уникальный номер ЕХ
    private final DoubleProperty sumDoc;
    private final ObjectProperty<LocalDate> operDayDate;
    private final StringProperty kindName;
    private final ObjectProperty<LocalDate> docDate;
    private final StringProperty branchName;
    private final ObjectProperty<DocStatus> docStatusName;

  public DocumentFX(String objectId, String docNumber, double sumDoc, LocalDate operDayDate,
                    String kindName, LocalDate docDate, String branchName, DocStatus docStatusName) {

        this.objectId = objectId == null ? "" : objectId;
        this.docNumber = new SimpleStringProperty(docNumber == null ? "" : docNumber);
        this.sumDoc = new SimpleDoubleProperty(sumDoc);
        this.operDayDate = new SimpleObjectProperty<>(operDayDate);
        this.kindName = new SimpleStringProperty(kindName == null ? "" : kindName);
        this.docDate = new SimpleObjectProperty<>(docDate);
        this.branchName = new SimpleStringProperty(branchName == null ? "" : branchName);
        this.docStatusName = new SimpleObjectProperty<>(docStatusName);
    }

    public String getObjectId() {
        return objectId;
    }

    public double getSumDoc() {
        return sumDoc.get();
    }
    public StringProperty sumDocProperty() {
        return new SimpleStringProperty( String.format("%,.2f", sumDoc.get()) );
    }

    public String getDocNumber() {
        return docNumber.get();
    }
    public StringProperty docNumberProperty() {
        return docNumber;
    }

    public LocalDate getOperDayDate() {
        return operDayDate.get();
    }
    public StringProperty operDayDateProperty() {
        return new SimpleStringProperty(operDayDate.get().format(displayFmt));
    }

    public String getKindName() {
        return kindName.get();
    }
    public StringProperty kindNameProperty() {
        return kindName;
    }

    public LocalDate getDocDate() {
        return docDate.get();
    }
    public StringProperty docDateProperty() {
        return new SimpleStringProperty(docDate.get().format(displayFmt));
    }

    public String getBranchName() {
        return branchName.get();
    }
    public StringProperty branchNameProperty() {
        return branchName;
    }

    public DocStatus getDocStatusName() {
        return docStatusName.get();
    }
    public StringProperty docStatusNameProperty() {
        return new SimpleStringProperty(docStatusName.get().getTitle());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentFX that = (DocumentFX) o;
        return getObjectId().equals(that.getObjectId()) &&
                getDocNumber().equals(that.getDocNumber()) &&
                getOperDayDate().equals(that.getOperDayDate()) &&
                getKindName().equals(that.getKindName()) &&
                getDocDate().equals(that.getDocDate()) &&
                getBranchName().equals(that.getBranchName()) &&
                getDocStatusName().equals(that.getDocStatusName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectId, docNumber, operDayDate, kindName, docDate, branchName, docStatusName);
    }

    @Override
    public String toString() {
        return "DocumentFX{" +
                "objectId='" + this.getObjectId() + '\'' +
                ", docNumber=" + this.getDocNumber() +
                ", sumDoc=" + this.getSumDoc() +
                ", operDayDate=" + this.getDocDate().format(displayFmt) +
                ", kindName=" + this.getKindName() +
                ", docDate=" + this.getDocDate().format(displayFmt) +
                ", branch=" + this.getBranchName() +
                ", docStatusName=" + this.getDocStatusName() +
                '}';
    }
}
