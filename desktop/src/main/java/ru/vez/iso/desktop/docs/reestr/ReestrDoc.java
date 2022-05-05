package ru.vez.iso.desktop.docs.reestr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Описание документа из файла REESTR, содержимого ZIP файла DIR.zip, открываемого пользователем в форме Documents
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReestrDoc implements Serializable {
    private String id;
    private String basePath;
    private List<ReestrFile> files;
    private ReestrData data;
}
