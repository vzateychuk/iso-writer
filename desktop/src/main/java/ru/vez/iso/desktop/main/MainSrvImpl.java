package ru.vez.iso.desktop.main;

import ru.vez.iso.desktop.model.ExStatus;
import ru.vez.iso.desktop.model.ExType;
import ru.vez.iso.desktop.model.OperatingDayFX;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainSrvImpl implements MainSrv {

    @Override
    public CompletableFuture<List<OperatingDayFX>> findOperatingDaysAsync(int period) {

        return CompletableFuture.supplyAsync(
                () -> {
                    List<OperatingDayFX> result = IntStream.range(1, period)
                            .mapToObj(i -> new OperatingDayFX(LocalDate.of(1900+i, i, i), ExType.CD, ExStatus.READY_WRITE))
                            .collect(Collectors.toList());

                    try {
                        Thread.sleep(period * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    return result;
                }
        );
    }
}
