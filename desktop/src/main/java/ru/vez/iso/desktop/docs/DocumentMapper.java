package ru.vez.iso.desktop.docs;

import ru.vez.iso.desktop.docs.reestr.ReestrDoc;

/**
 * Маппинг описания документа из Reestr-а в отображаемый в UI DocumentFX
 * */
public interface DocumentMapper {

    DocumentFX mapToDocFX(ReestrDoc d);
}
