package ru.vez.iso.desktop.document;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.util.Objects;

/**
 * DataModel "Документ Единицы хранения"
 * */
public class DocumentFX {

    private final String objectId;
    private final StringProperty docNumber;
    private final ObjectProperty<Double> sumDoc;
    private final ObjectProperty<LocalDate> operDayDate;
    private final ObjectProperty<DocType> docType;
    private final ObjectProperty<LocalDate> docDate;
    private final ObjectProperty<BranchType> branch;
    private final ObjectProperty<DocStatus> docStatusName;
    // uses in View when selecting the current row
    private BooleanProperty selected;

  public DocumentFX(String objectId, String docNumber, double sumDoc, LocalDate operDayDate,
      DocType docType, LocalDate docDate, BranchType branch, DocStatus docStatusName) {

        this.objectId = objectId;
        this.docNumber = new SimpleStringProperty(docNumber);
        this.sumDoc = new SimpleObjectProperty<>(sumDoc);
        this.operDayDate = new SimpleObjectProperty<>(operDayDate);
        this.docType = new SimpleObjectProperty<>(docType);
        this.docDate = new SimpleObjectProperty<>(docDate);
        this.branch = new SimpleObjectProperty<>(branch);
        this.docStatusName = new SimpleObjectProperty<>(docStatusName);
        this.selected = new SimpleBooleanProperty(false);
    }

    public String getObjectId() {
        return objectId;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }
    public Boolean isSelected() {
        return selected.get();
    }
    public BooleanProperty selectedProperty() {
        return selected;
    }

    public double getSumDoc() {
        return sumDoc.get();
    }
    public ObjectProperty<Double> sumDocProperty() {
        return sumDoc;
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
    public ObjectProperty<LocalDate> operDayDateProperty() {
        return operDayDate;
    }

    public DocType getDocType() {
        return docType.get();
    }
    public ObjectProperty<DocType> docTypeProperty() {
        return docType;
    }

    public LocalDate getDocDate() {
        return docDate.get();
    }
    public ObjectProperty<LocalDate> docDateProperty() {
        return docDate;
    }

    public BranchType getBranch() {
        return branch.get();
    }
    public ObjectProperty<BranchType> branchProperty() {
        return branch;
    }

    public DocStatus getDocStatusName() {
        return docStatusName.get();
    }
    public ObjectProperty<DocStatus> docStatusNameProperty() {
        return docStatusName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentFX that = (DocumentFX) o;
        return getObjectId().equals(that.getObjectId()) &&
                getDocNumber().equals(that.getDocNumber()) &&
                getOperDayDate().equals(that.getOperDayDate()) &&
                getDocType().equals(that.getDocType()) &&
                getDocDate().equals(that.getDocDate()) &&
                getBranch().equals(that.getBranch()) &&
                getDocStatusName().equals(that.getDocStatusName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectId, docNumber, operDayDate, docType, docDate, branch, docStatusName);
    }

    @Override
    public String toString() {
        return "DocumentFX{" +
                "objectId='" + objectId + '\'' +
                ", docNumber=" + docNumber +
                ", sumDoc=" + sumDoc +
                ", operDayDate=" + operDayDate +
                ", docType=" + docType +
                ", docDate=" + docDate +
                ", branch=" + branch +
                ", docStatusName=" + docStatusName +
                '}';
    }
}
