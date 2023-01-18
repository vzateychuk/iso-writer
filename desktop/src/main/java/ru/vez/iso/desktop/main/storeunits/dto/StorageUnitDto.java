package ru.vez.iso.desktop.main.storeunits.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * StorageUnitDto received from backend
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageUnitDto {

    private String objectId;        // "45352fba-8818-4fbe-8ee1-04c47a1c9f75",
    private StorageUnitOperDayDto operatingDay;  // object
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
    private String hashSum;
    private TypeStorUnitDto typeSu; // object
    private boolean isoPresent;     // flag if ISO already available
}
