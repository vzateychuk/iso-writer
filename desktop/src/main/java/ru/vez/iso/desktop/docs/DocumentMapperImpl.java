package ru.vez.iso.desktop.docs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.docs.reestr.ReestrData;
import ru.vez.iso.desktop.docs.reestr.ReestrDoc;

import java.time.LocalDate;

/**
 * Мапер из REESTR Doc в отображаемый DocumentFX
 * */
public class DocumentMapperImpl implements DocumentMapper {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public DocumentFX mapToDocFX(ReestrDoc d) {

        ReestrData rd = d.getData();

        DocType docType = this.getDocType(rd.getTypeName());
        BranchType branch = this.getBranchType(rd.getBranchId());
        DocStatus docStatus = this.getDocStatus(rd.getDocStatus());

        LocalDate operDayDate = LocalDate.parse(rd.getDocDate());
        LocalDate docDate = LocalDate.parse(rd.getDocDate());

        return new DocumentFX(
                            rd.getObjectId(),
                            rd.getDocNumber(),
                            rd.getDocSum(),
                            operDayDate,
                            docType,
                            docDate,
                            branch,
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

    private DocType getDocType(String docType) {

        try {
            return DocType.valueOf(docType);
        } catch (IllegalArgumentException ex) {
            logger.warn("unable to map DocType: {}", docType);
        }
        return DocType.UNKNOWN_TYPE;
    }

    //endregion
}
