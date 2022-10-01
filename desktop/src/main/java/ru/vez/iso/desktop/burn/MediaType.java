package ru.vez.iso.desktop.burn;

import com.ms.imapi2.IMAPI_MEDIA_PHYSICAL_TYPE;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.ms.imapi2.IMAPI_MEDIA_PHYSICAL_TYPE.*;

public enum MediaType {

    DVD("DVD", Arrays.asList(
            IMAPI_MEDIA_TYPE_DVDPLUSR, IMAPI_MEDIA_TYPE_DVDPLUSRW,
            IMAPI_MEDIA_TYPE_DVDDASHR, IMAPI_MEDIA_TYPE_DVDDASHRW
    )),
    CD("CD", Arrays.asList(IMAPI_MEDIA_TYPE_CDR, IMAPI_MEDIA_TYPE_CDRW)),
    DVD_CD("DVD/CD", Arrays.asList(
            IMAPI_MEDIA_TYPE_DVDPLUSR, IMAPI_MEDIA_TYPE_DVDPLUSRW, IMAPI_MEDIA_TYPE_DVDDASHR,
            IMAPI_MEDIA_TYPE_DVDDASHRW, IMAPI_MEDIA_TYPE_CDR, IMAPI_MEDIA_TYPE_CDRW
    ));

    private final String title;
    private final List<IMAPI_MEDIA_PHYSICAL_TYPE> physicalTypesAllowed;

    MediaType(String title, List<IMAPI_MEDIA_PHYSICAL_TYPE> physicalTypes) {
        this.title = title;
        this.physicalTypesAllowed = physicalTypes;
    }

    public String getTitle() {
        return title;
    }

    public List<IMAPI_MEDIA_PHYSICAL_TYPE> getPhysicalTypesAllowed() {
        return Collections.unmodifiableList(this.physicalTypesAllowed);
    }
}
