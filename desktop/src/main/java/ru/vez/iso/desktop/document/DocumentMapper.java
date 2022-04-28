package ru.vez.iso.desktop.document;

import ru.vez.iso.desktop.document.reestr.ReestrDoc;

/**
 * Маппинг описания документа из Reestr-а в отображаемый в UI DocumentFX
 * */
public interface DocumentMapper {

    DocumentFX mapToDocFX(ReestrDoc d);
}
