package ru.vez.iso.desktop.docs.reestr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Описание документов хранящихся на диске из файла REESTR-содержимого ZIP файла DIR.zip, открываемого пользователем в форме Documents
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reestr implements Serializable {
    private String storageUnitId;
    private List<ReestrDoc> docs;
}
