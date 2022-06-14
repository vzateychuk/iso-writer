package ru.vez.iso.desktop.main.operdays;

import ru.vez.iso.desktop.main.TypeSu;
import ru.vez.iso.desktop.main.operdays.dto.OperDayDto;
import ru.vez.iso.desktop.shared.DataMapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * OperationDayMapper used to map OperationDays service response to OperationDayFX list
 * */
public class OperationDayMapper implements DataMapper<OperDayDto, OperatingDayFX> {

    @Override
    public OperatingDayFX map(OperDayDto dto) {

        LocalDate date = LocalDate.parse( dto.getOperatingDayDate(), DateTimeFormatter.ISO_LOCAL_DATE );
        TypeSu typeSu = TypeSu.valueOf( dto.getTypeSu().getCode() );
        OperDayStatus status = OperDayStatus.valueOf( dto.getOperatingDayStatus() );
        LocalDate createdAt = LocalDate.parse( dto.getCreationDate(), DateTimeFormatter.ISO_LOCAL_DATE_TIME );

        return new OperatingDayFX(
                dto.getObjectId(),
                date,
                typeSu,
                status,
                createdAt
        );
    }
}
