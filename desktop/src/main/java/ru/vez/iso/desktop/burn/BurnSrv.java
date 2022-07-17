package ru.vez.iso.desktop.burn;

/**
 * Manages IMAPI operations:
 * Burning Disks
 * DriveInfo
 * */
public interface BurnSrv {

    /**
     * Examines disc device characteristics that are independent of media inserted in the device.
     * Uses Microsoft IMAPI2 interface
     * @link https://docs.microsoft.com/en-gb/windows/win32/imapi/checking-drive-support
     * */
    void recorderInfo();

}
