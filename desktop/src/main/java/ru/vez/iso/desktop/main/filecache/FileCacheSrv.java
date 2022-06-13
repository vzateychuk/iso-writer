package ru.vez.iso.desktop.main.filecache;

/**
 * Read/Write/Check operation with CD/DVD disks
 */
public interface FileCacheSrv {

    /**
     * Загрузить ISO файл в локальный файловый кэш
     * */
    void readFileCacheAsync(String dir);

    /**
     * Delete the file and invoke reload
     * */
    void deleteFileAndReload(String fileName);

    /**
     * Загрузить ISO файл в локальный файловый кэш
     */
    void loadISOAsync(String name);

}
