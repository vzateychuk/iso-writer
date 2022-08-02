package ru.vez.iso.desktop.shared;


public enum SettingType {

    /**
     * Название файла настроек
     * */
    SETTING_FILE("settings.properties"),

    /**
     * Путь к файловому кэшу
     * */
    ISO_CACHE_PATH("./temp"),

    /**
     * Период для получения списка операционных дней
     * */
    OPERATION_DAYS("30"),

    /**
     * Период автоматической перезагрузки списка операционных дней и EX
     * */
    REFRESH_PERIOD("5"),

    /**
     * API бэкэнд сервера
     * */
    BACKEND_API("http://localhost:8080/abdd/api"),

    /**
     * Дней хранятся файлы в кэше
     * */
    EVICT_CACHE_DAYS("30"),

    /**
     * Скорость прожига(записи) iso диска
     * */
    BURN_SPEED("1")
    ;

    private final String defaultValue;

    SettingType(String value) {
        this.defaultValue = value;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
