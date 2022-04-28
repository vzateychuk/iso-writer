package ru.vez.iso.desktop.document.reestr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Описание file документа из REESTR, содержимого ZIP файла DIR.zip, открываемого пользователем в форме Documents
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReestrFile implements Serializable {
    private ReestrFileType type;
    private String hash;
    private String path;
}
