package ru.vez.iso.desktop.main;

/**
 * Read/Write/Check operation with CD/DVD disks
 */
public interface CacheSrv {

    /**
     * Загрузить ISO файл в локальный файловый кэш
     * */
    void readFileCacheAsync(String dir);

    /**
     * Delete the file and invoke reload
     * */
    void deleteFileAndReload(String fileName);
}
