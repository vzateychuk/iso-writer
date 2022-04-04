package ru.vez.iso.desktop.disks;

public class DiskCtl {

    private final DisksService service;

    public DiskCtl(DisksService service) {
        this.service = service;
    }

    public void onWrite() {
        System.out.println("DiskCtl.onWrite");
    }

    public void onCheck() {
        System.out.println("DiskCtl.onCheck");
    }
}
