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
    private String objectId;    // 5ea709b2-f4c7-48ee-8ece-2dfcd90079ac,
    private String objectName;   // Документы операционного дня АБДД",
    private String creator;    // admin",
    private String modifier; // admin",
    private String creationDate; //2022-04-18T11:46:07.925891",
    private String modifyDate; //2022-04-18T12:03:23.082151",
    private boolean deleted; //":false,
    private String typeName; //DocPaymentAbdd",
    private boolean hasFile;    //":false,
    private String author; //ФИО автора",
    private String docNumber; //974838",
    private String docDate; //2021-06-22",
    private String controllerName; //ФИО контролера",
    private String controllerDate; //2021-06-22T15:59:59+07:00",
    private String packet; //Номер пачки",
    private String arm; //123",
    private String branchId; //209acf66-fed0-466f-8cf2-067609011e16",
    private String kindId; //4dd31a13-7428-4135-b5a0-43eda447ce42",
    private String externalObjectId; //1650257167",
    private String docStatus; //CREATED",
    private boolean correctionMark;  // ":false,
    private String executor; //Павлов Павел Павлович",
    private double docSum;  //   23171.56,
    private boolean deleteMark; //false,
    private String operationImportId; //e76de71c-1291-477f-ba5c-1db260fc9a39",

}
