package ru.vez.iso.desktop.main.operdays;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.main.operdays.dto.OperationDayDto;
import ru.vez.iso.desktop.shared.DataMapper;

/**
 * OperationDayMapper used to map OperationDays service response to OperationDayFX list
 * */
public class OperationDayMapper implements DataMapper<OperationDayDto, OperatingDayFX> {

    private static final Logger logger = LogManager.getLogger();
    @Override
    public OperatingDayFX map(OperationDayDto dto) {

        TypeSu typeSu;
        try {
            typeSu = TypeSu.valueOf( dto.getTypeSu().getCode() );
        } catch (IllegalArgumentException ex) {
            typeSu = TypeSu.UNKNOWN;
            logger.warn("Bad storageUnit type: {}", dto.getTypeSu().getCode(), ex);
        }

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
                typeSu,
                status,
                createdAt
        );
    }
}
