package ru.vez.iso.desktop.main;

import ru.vez.iso.desktop.model.ExStatus;
import ru.vez.iso.desktop.model.ExType;
import ru.vez.iso.desktop.model.OperatingDayFX;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class MainSrvImpl implements MainSrv {

    List<OperatingDayFX> list = Arrays.asList(
            new OperatingDayFX(LocalDate.now(), ExType.CD, ExStatus.READY_WRITE),
            new OperatingDayFX(LocalDate.now(), ExType.DVD, ExStatus.READY_WRITE),
            new OperatingDayFX(LocalDate.now(), ExType.CD, ExStatus.SIGNING)
    );

    @Override
    public List<OperatingDayFX> findByDays(int periodDays) {
        // TODO implement List<OperatingDayFX> findByDays
        return list;
    }
}
