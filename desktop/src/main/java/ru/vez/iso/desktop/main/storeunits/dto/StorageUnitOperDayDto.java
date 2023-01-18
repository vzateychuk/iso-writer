package ru.vez.iso.desktop.main.storeunits.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * StorageUnit contains OperationDayDto received from backend
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageUnitOperDayDto {
    private String objectId;        // "85fd35ba-4899-4de3-9986-8c8dea1edb55",
    private String objectName;      // "Операционный день АБДД",
    private String numberSu;        // "SUxXx948",
}
