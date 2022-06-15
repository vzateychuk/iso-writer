package ru.vez.iso.desktop.main.operdays;

import java.time.LocalDate;
import java.util.List;

/**
 * Service to get OperationDays from backend
 * */
public interface OperationDaysSrv {

    List<OperatingDayFX> loadOperationDays(LocalDate from);
}
