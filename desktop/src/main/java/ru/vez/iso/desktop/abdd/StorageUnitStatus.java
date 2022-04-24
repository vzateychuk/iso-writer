package ru.vez.iso.desktop.abdd;

/**
 * Статус ЕХ.
 * - "Готово для записи на внешний носитель",
 * - "Записано на внешний носитель"
 */
public enum StorageUnitStatus {

    DRAFT("Черновик"),				// Черновик
    PREPARING_RECORDING("Готовятся для записи"),
    READY_TO_RECORDING("Готово для записи на внешний носитель"),
    RECORDED("Записано на внешний носитель");

    private final String title;

    StorageUnitStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

}
