package ru.vez.iso.desktop.docs;

import ru.vez.iso.desktop.docs.reestr.ReestrDoc;

/**
 * Маппинг описания документа из Reestr-а в отображаемый в UI DocumentFX
 * */
public interface DocMapper {

    DocumentFX mapToDocFX(ReestrDoc d);
}
