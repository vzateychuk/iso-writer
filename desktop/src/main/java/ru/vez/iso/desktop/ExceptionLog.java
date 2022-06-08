package ru.vez.iso.desktop;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * There is a ExceptionLogger to log all Uncaught Runtime exception
 * */
public class ExceptionLog implements Thread.UncaughtExceptionHandler {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public void uncaughtException(Thread t, Throwable ex) {
        logger.error("Stacktrace:", ex);
    }
}
