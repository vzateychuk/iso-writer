package ru.vez.iso.desktop.main;

import javafx.collections.ObservableMap;
import lombok.extern.java.Log;
import ru.vez.iso.desktop.model.ExStatus;
import ru.vez.iso.desktop.model.ExType;
import ru.vez.iso.desktop.model.OperatingDayFX;
import ru.vez.iso.desktop.state.AppStateData;
import ru.vez.iso.desktop.state.AppStateType;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log
public class MainSrvImpl implements MainSrv {

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final Executor exec;
    private Future<Void> future = CompletableFuture.allOf();

    public MainSrvImpl(ObservableMap<AppStateType, AppStateData> appState, Executor exec) {
        this.appState = appState;
        this.exec = exec;
    }

    @Override
    public void loadOperatingDaysAsync(int period) {

        // Avoid multiply pressing
        if (!future.isDone()) {
            log.info("Async operation in progress, skipping");
            return;
        }

        log.info("getOperatingDaysAsync. period: " + period);
        future = CompletableFuture.supplyAsync(() -> getListWithDelay(period), exec)
                .thenAccept(opsDay -> appState.put(
                        AppStateType.OPERATION_DAYS, AppStateData.builder().value(opsDay).build()
                ));
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
                .mapToObj(i -> {
                    LocalDate date = LocalDate.of(1900+i, i+1, i+1);
                    return new OperatingDayFX(String.valueOf(i), date, ExType.CD, ExStatus.READY_WRITE, date, i%2==0);
                })
                .collect(Collectors.toList());
    }

    //endregion
}
