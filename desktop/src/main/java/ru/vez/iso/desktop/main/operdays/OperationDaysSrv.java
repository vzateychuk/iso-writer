package ru.vez.iso.desktop.main.operdays;

import java.time.LocalDate;
import java.util.List;

/**
 * Service to get Operation Days
 * */
public interface OperationDaysSrv {

    List<OperatingDayFX> loadOperationDays(LocalDate from);
}
