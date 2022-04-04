package ru.vez.iso.desktop.main;

/**
* Main controller for logged user
*/
public class MainCtl {

    private final MainSrv service;

    public MainCtl(MainSrv service) {
        this.service = service;
    }

    public void onSubmit() {
        System.out.println("MainCtl.onSubmit");
    }
}
