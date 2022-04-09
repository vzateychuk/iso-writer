package ru.vez.iso.desktop.main;

import lombok.extern.java.Log;
import ru.vez.iso.desktop.model.ExStatus;
import ru.vez.iso.desktop.model.ExType;
import ru.vez.iso.desktop.model.OperatingDayFX;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log
public class MainSrvImpl implements MainSrv {

    private final Executor exec;

    public MainSrvImpl(Executor exec) {
        this.exec = exec;
    }

    @Override
    public CompletableFuture<List<OperatingDayFX>> findOperatingDaysAsync(int period) {

        log.info("findOperatingDaysAsync: " + Thread.currentThread().getName());
        return CompletableFuture.supplyAsync(() -> getListWithDelay(period), exec);
    }

    //region PRIVATE

    private List<OperatingDayFX> getListWithDelay(int period) {

        log.info("getListWithDelay: " + Thread.currentThread().getName());
        try {
            Thread.sleep(period * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return IntStream.rangeClosed(0, period)
                .mapToObj(i -> new OperatingDayFX(LocalDate.of(1900+i, i+1, i+1), ExType.CD, ExStatus.READY_WRITE))
                .collect(Collectors.toList());
    }

    //endregion
}
