package ru.vez.iso.desktop.main.storeunits;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitDto;
import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitOperDayDto;
import ru.vez.iso.desktop.shared.DataMapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * StorageUnitMapper used to map StorageUnit (from backend response) to StorageUnitFX list
 * */
public class StorageUnitMapper implements DataMapper<StorageUnitDto, StorageUnitFX> {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public StorageUnitFX map(StorageUnitDto dto) {

        StorageUnitStatus storageUnitStatus;
        try {
            storageUnitStatus = StorageUnitStatus.valueOf( dto.getStorageUnitStatus() );
        } catch (IllegalArgumentException ex) {
            storageUnitStatus = StorageUnitStatus.UNKNOWN_STATUS;
            logger.warn("Bad storageUnit status: {}", dto.getStorageUnitStatus(), ex);
        }

        LocalDate creationDate = dto.getCreationDate() != null
                ? LocalDate.parse( dto.getCreationDate(), DateTimeFormatter.ISO_LOCAL_DATE_TIME )
                : null;

        LocalDate savingDate = dto.getSavingDate() != null
                ? LocalDate.parse( dto.getSavingDate(), DateTimeFormatter.ISO_LOCAL_DATE_TIME )
                : null;

        Long dataSize = Optional.ofNullable(dto.getCapacity()).orElse(0L);

        StorageUnitOperDayDto operDayDto = dto.getOperatingDay();
        String operDayId;
        if (operDayDto != null) {
            operDayId = operDayDto.getObjectId();
        } else {
            logger.warn("Bad operationDay value = NULL in StorageUnit: '{}'", dto.getObjectId());
            operDayId = "";
        }

        return new StorageUnitFX (
                dto.getObjectId(),
                operDayId,
                dto.getNumberSu(),
                creationDate,
                dataSize,
                dto.getStorageDate(),
                storageUnitStatus,
                savingDate,
                dto.isDeleted(),
                dto.isIsoPresent()
        );
    }
}
