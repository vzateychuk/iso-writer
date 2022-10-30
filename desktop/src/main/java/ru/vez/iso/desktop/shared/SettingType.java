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
    BACKEND_API("http://172.29.73.102:8080"),

    /**
     * Дней хранятся файлы в кэше
     * */
    EVICT_CACHE_DAYS("30"),

    /**
     * Скорость прожига(записи) iso диска
     * */
    BURN_SPEED("1"),

    /**
     * Адрес сервера аутентификации
     * */
    AUTH_API("http://172.29.73.59:8080"),

    /**
     * API path аутентификации
     * */
    AUTH_PATH("/realms/ABDD/protocol/openid-connect/token"),

    /**
     * ClientId параметр используемый при KeyCloak login
     * */
    CLIENT_ID("abdd-client"),

    /**
     * GrandType параметр используемый при KeyCloak login
     * */
    GRANT_TYPE("password"),

    /**
     * ClientSecret параметр используемый при KeyCloak login
     * */
    CLIENT_SECRET("To2MJyImPgjZ9dL5YZhzY6gQUX1PkqkC"),
    ;

    private final String defaultValue;

    SettingType(String value) {
        this.defaultValue = value;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
