package ru.vez.iso.desktop.controllers;

import ru.vez.iso.desktop.View;
import ru.vez.iso.desktop.ViewSwitcher;

public class Login {

    public void onLogin() {
        // Switch to Main view
        ViewSwitcher.switchTo(View.MAIN);
    }
}
