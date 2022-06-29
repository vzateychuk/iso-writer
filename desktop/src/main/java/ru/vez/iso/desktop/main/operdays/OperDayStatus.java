package ru.vez.iso.desktop.main.operdays;

/**
 * Статус операционного дня
 * */
public enum OperDayStatus {

    NEW("Новый"),
    RECORDED("Записан"),
    READY_TO_SIGN("Готов к подписанию"),
    SIGNING("На подписании"),
    SIGNED("Подписан"),
    READY_TO_RECORDING("Готов для записи на EX"),
    UNKNOWN("Неопределен");

    private final String title;

    OperDayStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

}
