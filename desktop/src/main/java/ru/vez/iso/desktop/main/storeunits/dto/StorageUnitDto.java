package ru.vez.iso.desktop.main.storeunits.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * StorageUnitDto received from backend
 * Example:
 *       {
 *                 "objectId": "7ace4727-1e5e-43ff-8206-19149aace31d",
 *                 "objectName": null,
 *                 "versionId": null,
 *                 "creator": "admin",
 *                 "modifier": "admin",
 *                 "creationDate": "2022-04-08T15:08:06.509687",
 *                 "modifyDate": "2022-05-30T19:04:40.345819",
 *                 "objectState": null,
 *                 "acl": {},
 *                 "deleted": false,
 *                 "typeName": "StorageUnitAbdd",
 *                 "hasFile": false,
 *                 "timeStamp": null,
 *                 "operatingDayId": "03f8fae4-5057-4195-866d-19704e37c9c7",
 *                 "numberSu": "2",
 *                 "capacity": 512000,
 *                 "branchId": "aec7e48b-b1e8-4720-8e46-db3d3c1a6398",
 *                 "storageDate": "100",
 *                 "savingDate": "2022-05-20T18:54:02.06887",
 *                 "hashSum": null,
 *                 "hashAlgorithm": null,
 *                 "savingLocation": "initValue2",
 *                 "savingLocationReserv": "initValue2",
 *                 "storageUnitStatus": "RECORDED",
 *                 "typeSu": {
 *                     "elementId": "2acf6c5d-d35e-4c58-a2d7-394955af2c22",
 *                     "code": "ABC",
 *                     "elementName": "elem",
 *                     "shortName": "short",
 *                     "typeName": "StorageUnitTypeAbdd",
 *                     "creationDate": "2021-11-18T09:20:21.173825",
 *                     "modifyDate": "2022-04-26T16:39:01.626341",
 *                     "deleted": true,
 *                     "creator": null,
 *                     "modifier": "admin",
 *                     "capacity": 629145600,
 *                     "isDefault": false,
 *                     "savingLocation": "initValue",
 *                     "savingLocationReserv": "initValue"
 *                 },
 *                 "errorMessage": "test"
 *       },
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageUnitDto {

    private String objectId;        // "45352fba-8818-4fbe-8ee1-04c47a1c9f75",
    private String operatingDayId;  // "03f8fae4-5057-4195-866d-19704e37c9c7"
    private String objectName;      // "Операционный день АБДД",
    private String typeName;        // StorageUnitAbdd
    private boolean hasFile;        // false,
    private String numberSu;        // "2",
    private String creationDate;    // "2022-05-25T10:40:45.739907"
    private String storageUnitStatus;
    private String savingDate;
    private boolean deleted;        //
    private String storageDate;
    private Long capacity;
    private TypeStorUnitDto typeSu; // object
}
