package ru.vez.iso.desktop.main;

import ru.vez.iso.desktop.model.OperatingDayFX;

import java.util.List;

public interface MainSrv {

     List<OperatingDayFX> findOperatingDays(int period);

}
