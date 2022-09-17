package ru.vez.iso.desktop.shared;

import java.time.format.DateTimeFormatter;

public class MyConst {

    private MyConst() { }

    public static final String UNZIP_FOLDER = "unzipped";
    public static final String REESTR_FILE = "REESTR.json";
    public static final String DIR_ZIP_FILE = "DIR.zip";
    public static final String CHECKSUM_FILE = "checksum.txt";

    public static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Название алгоритма получение HASH суммы по ГОСТ
    public static final String ALGO_GOST = "GOST3411-2012-512";
    public static final String SHA256 = "SHA-256";
}
