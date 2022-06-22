package ru.vez.iso.desktop.main.filecache;

import ru.vez.iso.desktop.shared.IsoFileFX;

import java.util.List;

/**
 * Read/Write/Check operation with CD/DVD disks
 */
public interface FileCacheSrv {

    /**
     * Загрузить ISO файл в локальный файловый кэш.
     * */
    List<IsoFileFX> readFileCache(String dir);

    /**
     * Delete the file
     * */
    String deleteFile(String fileName);

}
