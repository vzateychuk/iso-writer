package ru.vez.iso.desktop.burn;

import java.nio.file.Path;

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

    /**
     * Burn disk with directory path
     *
     * @param recorderIndex - this is index of the recorder
     * @param speed - burning speed
     * @param filepath - directory path for files
     * @param discTitle - burned disk label
     * */
    void burn(int recorderIndex, int speed, Path filepath, String discTitle);

    /**
     * Open burner's tray
     * */
    void openTray(int recorderIndex);
}
