package ru.vez.iso.desktop.docs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.docs.reestr.ReestrData;
import ru.vez.iso.desktop.docs.reestr.ReestrDoc;

import java.time.LocalDate;

/**
 * Мапер из REESTR Doc в отображаемый DocumentFX
 * */
public class DocMapperImpl implements DocMapper {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public DocumentFX mapToDocFX(ReestrDoc d) {

        ReestrData rd = d.getData();

        DocStatus docStatus = this.getDocStatus(rd.getDocStatus());

        LocalDate operDayDate = LocalDate.parse(rd.getDocDate());
        LocalDate docDate = LocalDate.parse(rd.getDocDate());

        return new DocumentFX(
                            rd.getObjectId(),
                            rd.getDocNumber(),
                            rd.getDocSum(),
                            operDayDate,
                            rd.getKindName(),
                            docDate,
                            rd.getBranchName(),
                            docStatus);
    }

    //region PRIVATE

    private DocStatus getDocStatus(String docStatus) {

        try {
            return DocStatus.valueOf(docStatus);
        } catch (IllegalArgumentException ex) {
            logger.warn("unable to map DocStatus: {}", docStatus);
        }
        return DocStatus.UNKNOWN_STATE;
    }

    private BranchType getBranchType(String branchType) {

        try {
            return BranchType.valueOf(branchType);
        } catch (IllegalArgumentException ex) {
            logger.warn("unable to map BranchType: {}", branchType);
        }
        return BranchType.UNKNOWN_BRANCH;
    }

    //endregion
}
