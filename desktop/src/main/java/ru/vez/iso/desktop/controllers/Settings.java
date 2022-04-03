package ru.vez.iso.desktop.controllers;

import javafx.event.ActionEvent;
import ru.vez.iso.desktop.View;
import ru.vez.iso.desktop.ViewSwitcher;

public class Settings {

    public void onSave(ActionEvent ev) {
        System.out.printf("Event: '%s'\n", ev);

        // Switch view
        ViewSwitcher.switchTo(View.MAIN);
    }

    public void onCancel(ActionEvent ev) {
        System.out.printf("Event: '%s'\n", ev);

        // Switch view
        ViewSwitcher.switchTo(View.MAIN);
    }
}
