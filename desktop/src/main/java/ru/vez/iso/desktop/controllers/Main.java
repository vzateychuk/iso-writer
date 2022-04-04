package ru.vez.iso.desktop.controllers;

import javafx.event.ActionEvent;
import ru.vez.iso.desktop.View;
import ru.vez.iso.desktop.ViewSwitcher;
import ru.vez.iso.desktop.repo.NoopDAOImpl;
import ru.vez.iso.shared.dao.ExCardDAO;
import ru.vez.iso.shared.model.ExCard;
import ru.vez.iso.shared.model.ExState;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    private ExCardDAO dao;

    public Main() {
        Map<Long, ExCard> store = IntStream.range(0,10).mapToObj(i ->
                new ExCard((long) i, "name"+i, ExState.READY, LocalDateTime.now(), "desc " + i))
                .collect(Collectors.toMap(ExCard::getId, Function.identity()));
        this.dao = new NoopDAOImpl(store);
    }

    public void onSettings(ActionEvent ev) {
        System.out.printf("Event: '%s'\n", ev);

        // Switch view
        ViewSwitcher.switchTo(View.SETTINGS);
    }

    public void onLogin(ActionEvent ev) {
        System.out.printf("Event: '%s'\n", ev);

        ViewSwitcher.switchTo(View.LOGIN);
    }

    public void onLogout(ActionEvent ev) {
        System.out.printf("Event: '%s'\n", ev);
    }
}
