package ru.vez.iso.desktop.disks;

import lombok.extern.java.Log;

@Log
public class DiskCtl {

    private final DisksSrv service;

    public DiskCtl(DisksSrv service) {
        this.service = service;
    }

    public void onWrite() {
        log.info("DiskCtl.onWrite");
    }

    public void onCheck() {
        log.info("DiskCtl.onCheck");
    }
}
