package ru.vez.iso.desktop.docs;

import java.nio.file.Path;

public interface DocumentSrv {

    /**
     * Загрузка из dirZip списка документов REESTR.json
     *
     * @param dirZip - полный путь к файлу архиву DIR.zip
     * */
    void loadAsync(Path dirZip);

    /**
     * Проверка checkSum с dirZip
     * @throws RuntimeException если нет возможности прочитать данные из какого либо из источников
     *
     * @param checksumFile - путь к файлу checksum.txt
     * @param dirZip - путь к файлу DIR.zip
     * @return true if checksums are equal
     * */
    boolean compareCheckSum(Path checksumFile, Path dirZip);
}
