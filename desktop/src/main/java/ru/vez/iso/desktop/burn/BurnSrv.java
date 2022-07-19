package ru.vez.iso.desktop.burn;

/**
 * Интерфейс для взаимодействия с библиотекой записи (burn) диска
 * Manages IMAPI operations
 * */
public interface BurnSrv {

    /**
     * Examines disc device characteristics that are independent of media inserted in the device.
     * Uses Microsoft IMAPI2 interface
     * @link https://docs.microsoft.com/en-gb/windows/win32/imapi/checking-drive-support
     * */
    RecorderInfo recorderInfo(int recorderIndex);

    void startBurn(String objectId);
}
