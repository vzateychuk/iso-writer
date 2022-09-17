package ru.vez.iso.desktop.docs;

import java.nio.file.Path;
import java.util.List;

public interface DocSrv {

    /**
     * Загрузка из dirZip списка документов REESTR.json
     *
     * @param dirZip - полный путь к файлу архиву DIR.zip
     * */
    List<DocumentFX> loadREESTR(Path dirZip);

    /**
     * Расчитывает checkSum файла по алгоритму algo
     * @throws RuntimeException если нет возможности прочитать данные
     *
     * @param path - путь к файлу
     * @param algo - алгоритм, например MyConst.ALGO_GOST
     * */
    String calculateFileHash(Path path, String algo);

    /**
     * Зачитывает содержимое файла
     *
     * @param filePath - путь к файлу
     * */
    String readFile(Path filePath);
}
