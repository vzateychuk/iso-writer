package ru.vez.iso.desktop.disks;

import javafx.event.ActionEvent;
import lombok.extern.java.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Log
public class DiskCtl {

    private static Logger logger = LogManager.getLogger();

    private final DisksSrv service;

    public DiskCtl(DisksSrv service) {
        this.service = service;
    }

    public void onWrite() {
        logger.debug("DiskCtl.onWrite");
    }

    public void onCheck() {
        logger.debug("DiskCtl.onCheck");
    }

    public void onWriteCopy(ActionEvent ev) {
        logger.debug("DiskCtl.onWriteCopy");
    }
}
