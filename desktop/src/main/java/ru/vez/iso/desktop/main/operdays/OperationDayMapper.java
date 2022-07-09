package ru.vez.iso.desktop.main.operdays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.main.operdays.dto.OperationDayDto;
import ru.vez.iso.desktop.shared.DataMapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * OperationDayMapper used to map OperationDays service response to OperationDayFX list
 * */
public class OperationDayMapper implements DataMapper<OperationDayDto, OperatingDayFX> {

    private static final Logger logger = LogManager.getLogger();
    @Override
    public OperatingDayFX map(OperationDayDto dto) {

        OperDayStatus status;
        try {
            status = OperDayStatus.valueOf( dto.getOperatingDayStatus() );
        } catch (IllegalArgumentException ex) {
            status = OperDayStatus.UNKNOWN;
            logger.warn("Bad OperDayStatus: {}", dto.getTypeSu().getCode(), ex);
        }

        LocalDate createdAt = LocalDate.parse( dto.getCreationDate(), DateTimeFormatter.ISO_LOCAL_DATE_TIME );
        LocalDate date = LocalDate.parse( dto.getOperatingDayDate(), DateTimeFormatter.ISO_LOCAL_DATE );

        return new OperatingDayFX(
                dto.getObjectId(),
                date,
                dto.getTypeSu().getElementName(),
                status,
                createdAt
        );
    }
}
