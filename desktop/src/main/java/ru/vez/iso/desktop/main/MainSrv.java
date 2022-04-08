package ru.vez.iso.desktop.main;

import ru.vez.iso.desktop.model.OperatingDayFX;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MainSrv {

     CompletableFuture<List<OperatingDayFX>> findOperatingDaysAsync(int period);

}
