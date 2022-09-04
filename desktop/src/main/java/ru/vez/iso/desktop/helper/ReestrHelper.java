package ru.vez.iso.desktop.helper;

import ru.vez.iso.desktop.docs.reestr.RFileType;
import ru.vez.iso.desktop.docs.reestr.Reestr;
import ru.vez.iso.desktop.docs.reestr.ReestrDoc;
import ru.vez.iso.desktop.docs.reestr.ReestrFile;

public class ReestrHelper {

    private ReestrHelper() {
    }

    public static ReestrFile findFileInReestrOrException(Reestr reestr, String docId, RFileType fileType) {
        ReestrDoc reestrDoc = reestr.getDocs().stream()
                .filter(d -> d.getData().getObjectId().equals(docId))
                .findAny()
                .orElseThrow(() -> new RuntimeException("not found in REESTR. DocId" + docId));
        ReestrFile found = reestrDoc.getFiles().stream()
                .filter(f -> f.getType().equals(fileType))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Not found in REESTR: FileType: " + fileType.getTitle()));
        return found;
    }


}
