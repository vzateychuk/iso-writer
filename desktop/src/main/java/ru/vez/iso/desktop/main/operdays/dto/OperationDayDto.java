package ru.vez.iso.desktop.main.operdays.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
* OperationDay DTO received from backend
 * Example:
 *       {
 *         "objectId": "03f8fae4-5057-4195-866d-19704e37c9c7",
 *         "objectName": "Операционный день АБДД",
 *         "versionId": null,
 *         "creator": "admin",
 *         "modifier": "admin",
 *         "creationDate": "2022-05-20T14:10:43.472306",
 *         "modifyDate": "2022-05-20T17:39:56.394185",
 *         "objectState": null,
 *         "acl": {},
 *         "deleted": false,
 *         "typeName": "OperatingDayAbdd",
 *         "hasFile": false,
 *         "timeStamp": null,
 *         "uploadingOperationId": null,
 *         "numberSu": "2022-05-19_new1",
 *         "operatingDayDate": "2022-05-19",
 *         "typeSu": {
 *           "elementId": "e806dd0f-e563-460f-8d66-1f2350b6e6f1",
 *           "code": "CD",
 *           "elementName": "CD/DVD Диск"
 *         },
 *         "operatingDayStatus": "READY_TO_SIGN"
 *       },
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationDayDto {

    private String objectId;        // "45352fba-8818-4fbe-8ee1-04c47a1c9f75",
    private String objectName;      // "Операционный день АБДД",
    private String typeName;        // OperatingDayAbdd
    private boolean hasFile;        // false,
    private String numberSu;        // "2022-04-05new1",
    private String operatingDayDate;     // "2022-04-05",
    private String creationDate;    // "2022-05-25T10:40:45.739907"
    private TypeSuDto typeSu;       // Object
    private String operatingDayStatus;   // "READY_TO_SIGN","NEW","SIGNED",...
}
