package ru.vez.iso.desktop.abdd;

/**
 * Статус операционного дня
 * */
public enum OpsDayStatus {

    SIGNING("На подписании"),
    READY_TO_RECORDING("Готов для записи на EX");

    private final String title;

    OpsDayStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

}
