package ru.vez.iso.desktop.disks;

public class DiskCtl {

    private final DisksSrv service;

    public DiskCtl(DisksSrv service) {
        this.service = service;
    }

    public void onWrite() {
        System.out.println("DiskCtl.onWrite");
    }

    public void onCheck() {
        System.out.println("DiskCtl.onCheck");
    }
}
