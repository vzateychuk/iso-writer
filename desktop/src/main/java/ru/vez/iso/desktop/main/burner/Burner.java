package ru.vez.iso.desktop.main.burner;

/**
 * Интерфейс для взаимодействия с библиотекой записи (burn) диска
 * */
public interface Burner {

    /**
     * Start burn
     * */
    void startBurn(String objectId);
}
