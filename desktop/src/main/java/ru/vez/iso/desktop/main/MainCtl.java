package ru.vez.iso.desktop.main;

/**
* Main controller for logged user
*/
public class MainCtl {

    private final MainService service;

    public MainCtl(MainService service) {
        this.service = service;
    }

    public void onSubmit() {
        System.out.println("MainCtl.onSubmit");
    }
}
