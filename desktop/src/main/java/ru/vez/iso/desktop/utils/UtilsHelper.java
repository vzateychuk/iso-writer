package ru.vez.iso.desktop.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UtilsHelper {

    private static final Logger logger = LogManager.getLogger();

    private UtilsHelper() {}

    public static void makeDelaySec(int delay) {
        try {
            Thread.sleep(delay * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * Parse text value to int. Default value will be used if parse fails
     *
     * @param text - value will be parsed to Int
     * @param defaultValue - value will be parsed if the text fails to parse
     * @return parsed value
     * */
    public static int parseIntOrDefault(String text, String defaultValue) {
        int val = Integer.parseInt(defaultValue);
        try{
            val = Integer.parseInt(text);
        } catch (NumberFormatException ex){
            logger.warn("unable to convert to int, value: " + text);
        }
        return val;
    }


}
