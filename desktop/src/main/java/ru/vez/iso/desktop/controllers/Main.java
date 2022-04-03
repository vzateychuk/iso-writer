package ru.vez.iso.desktop.controllers;

import javafx.event.ActionEvent;
import ru.vez.iso.desktop.View;
import ru.vez.iso.desktop.ViewSwitcher;

public class Main {

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
