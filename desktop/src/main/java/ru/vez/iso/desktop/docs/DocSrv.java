package ru.vez.iso.desktop.docs;

import java.io.IOException;
import java.nio.file.Path;

public interface DocSrv {

    /**
     * Загрузка из dirZip списка документов REESTR.json
     *
     * @param dirZip - полный путь к файлу архиву DIR.zip
     * */
    void loadAsync(Path dirZip);

    /**
     * Расчитывает checkSum файла dirZip
     * @throws RuntimeException если нет возможности прочитать данные
     *
     * @param dirZip - путь к файлу DIR.zip
     * */
    String getFileHash(Path dirZip) throws IOException;

}
