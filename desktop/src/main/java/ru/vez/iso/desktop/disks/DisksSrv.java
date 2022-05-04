package ru.vez.iso.desktop.disks;

public interface DisksSrv {

    /**
     * Загрузить ISO файл в локальный файловый кэш
     * */
    void readIsoFileNamesAsync(String dir);

    /**
     * Delete the file and invoke reload
     * */
    void deleteFileAndReload(String fileName);
}
