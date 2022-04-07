package ru.vez.iso.desktop.main;

import ru.vez.iso.desktop.model.ExStatus;
import ru.vez.iso.desktop.model.ExType;
import ru.vez.iso.desktop.model.OperatingDayFX;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainSrvImpl implements MainSrv {

    @Override
    public List<OperatingDayFX> findOperatingDays(int period) {

        try {
            Thread.sleep(period * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<OperatingDayFX> result = IntStream.rangeClosed(0, period)
                .mapToObj(i -> new OperatingDayFX(LocalDate.of(1900+i, i+1, i+1), ExType.CD, ExStatus.READY_WRITE))
                .collect(Collectors.toList());

        return result;
    }

}
