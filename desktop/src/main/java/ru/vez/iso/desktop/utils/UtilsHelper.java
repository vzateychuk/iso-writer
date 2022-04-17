package ru.vez.iso.desktop.utils;

public class UtilsHelper {

    private UtilsHelper() {}

    public static void makeDelaySec(int delay) {
        try {
            Thread.sleep(delay * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
