package ru.vez.iso.desktop.shared;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Observable;

public class MessageSrvImpl extends Observable implements MessageSrv {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public void news(String news) {
        logger.debug(news);
        setChanged();
        notifyObservers(news);
    }

}
