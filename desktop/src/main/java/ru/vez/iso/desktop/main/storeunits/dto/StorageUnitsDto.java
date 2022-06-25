package ru.vez.iso.desktop.main.storeunits.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * OperationDays DTO wrapper received from backend
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageUnitsDto {

    private int count;
    private List<StorageUnitDto> objects;
}
