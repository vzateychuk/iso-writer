package ru.vez.iso.desktop.main.storeunits.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  Example "typeSu": {
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
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeStorUnitDto {

    private String elementId;
    private Long capacity;
    private String elementName;
}
