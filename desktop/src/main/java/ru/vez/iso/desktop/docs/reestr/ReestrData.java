package ru.vez.iso.desktop.docs.reestr;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Данные документов операционного дня АБДД
 * Из файла REESTR, содержимого ZIP файла DIR.zip, открываемого пользователем в форме Documents
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReestrData implements Serializable {
    private String objectId;        // UUID документа,
    private String objectName;      // Документы операционного дня АБДД",
    private String creationDate;    // Дата/Время создания,
    private boolean deleted;        // Признак удаления документа
    private String kindName;        // Вид документа,
    private boolean hasFile;        // false,
    private String author;          // ФИО автора,
    private String docNumber;       // Номер документа,
    private String docDate;         // Дата документа,
    private String branchName;      // Название подразделения,
    private String externalObjectId; // Внешний идентификатор,
    private String docStatus;       // CREATED,
    private boolean correctionMark; // Признак "Исправлен",
    private double docSum;          // Сумма документа,
}
