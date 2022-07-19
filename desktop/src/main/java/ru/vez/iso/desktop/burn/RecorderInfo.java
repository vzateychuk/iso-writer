package ru.vez.iso.desktop.burn;

import com.ms.imapi2.IMAPI_FORMAT2_DATA_MEDIA_STATE;
import com.ms.imapi2.IMAPI_MEDIA_PHYSICAL_TYPE;
import lombok.Builder;
import lombok.Data;

import java.util.Optional;

import static com.ms.imapi2.IMAPI_FORMAT2_DATA_MEDIA_STATE.IMAPI_FORMAT2_UNKNOWN;
import static com.ms.imapi2.IMAPI_MEDIA_PHYSICAL_TYPE.IMAPI_MEDIA_TYPE_UNKNOWN;

@Data
@Builder
public class RecorderInfo {

    private static final String END_LINE = System.getProperty("line.separator");
    private static final String SP = "; ";

    private int legacyDeviceNumber;     // Gets the legacy 'device number' associated with the recorder.  This number is not guaranteed to be static.
    private String vendorId;            // The vendor ID in the device's INQUIRY data.
    private String productId;           // The Product ID in the device's INQUIRY data.
    private String productRevision;     // The Product Revision in the device's INQUIRY data.
    private String volumeName;          // Get the unique volume name (this is not a drive letter).
    private boolean deviceCanLoadMedia; // Gets whether the device can load the media tray
    private boolean isRecorderSupported;        // Determines if the recorder object supports the given format
    private boolean isCurrentMediaSupported;    // Determines if the current media in a supported recorder object supports the given format
    private IMAPI_FORMAT2_DATA_MEDIA_STATE mediaState; // The state (usability) of the current media
    private IMAPI_MEDIA_PHYSICAL_TYPE mediaType;    // Physical type of the optical media

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Recorder: ").append(vendorId).append(SP).append(productId).append(END_LINE)
                .append("Device number: ").append(legacyDeviceNumber).append(END_LINE)
                .append( "Vendor Id: ").append(vendorId).append(END_LINE)
                .append( "Product Id: ").append(productId).append(END_LINE)
                .append( "Product Revision: ").append(productRevision).append(END_LINE)
                .append( "VolumeName: ").append(volumeName).append(END_LINE)
                .append( "Can Load Media: ").append(deviceCanLoadMedia).append(END_LINE)
        .append(isRecorderSupported ? "Recorder IS supported." : "Recorder IS NOT supported.").append(END_LINE)
        .append(isCurrentMediaSupported ? "Media IS supported." : "Media IS NOT supported.").append(END_LINE);

        // Check a few MediaState possibilities. Each status is associated with a bit and some combinations are legal.
        String mediaStatus;
        mediaState = Optional.ofNullable(mediaState).orElse( IMAPI_FORMAT2_UNKNOWN );
        switch (mediaState) {
            case IMAPI_FORMAT2_DATA_MEDIA_STATE_BLANK_AND_APPENDABLE:
                mediaStatus = "Media is blank and appendable";
                break;
            case IMAPI_FORMAT2_DATA_MEDIA_STATE_PROTECTED_AND_UNSUPPORTED:
                mediaStatus = "Protected and unsupported";
                break;
            case IMAPI_FORMAT2_DATA_MEDIA_STATE_BLANK:
                mediaStatus = "Blank";
                break;
            case IMAPI_FORMAT2_DATA_MEDIA_STATE_UNKNOWN:
                mediaStatus = "Media state is Unknown.";
                break;
            case IMAPI_FORMAT2_DATA_MEDIA_STATE_OVERWRITE_ONLY :
                mediaStatus = "Currently, only overwriting is supported.";
                break;
            case IMAPI_FORMAT2_DATA_MEDIA_STATE_APPENDABLE  :
                mediaStatus = "Media is currently appendable.";
                break;
            case IMAPI_FORMAT2_DATA_MEDIA_STATE_FINAL_SESSION  :
                mediaStatus = "Media is in final writing session.";
                break;
            case IMAPI_FORMAT2_DATA_MEDIA_STATE_DAMAGED  :
                mediaStatus = "Media is damaged.";
                break;
            default:
                mediaStatus = "Unable to determine MediaStatus";
        }
        sb.append("Media Status: ").append(mediaStatus).append(END_LINE);

        // Check a few CurrentMediaType
        String mediaTypeName;
        this.mediaType = Optional.ofNullable(this.mediaType).orElse( IMAPI_MEDIA_TYPE_UNKNOWN );
        switch (this.mediaType) {
            case IMAPI_MEDIA_TYPE_UNKNOWN:
                mediaTypeName = "Empty device or an unknown disc type.";
                break;
            case IMAPI_MEDIA_TYPE_CDROM:
                mediaTypeName = "CD-ROM";
                break;
            case IMAPI_MEDIA_TYPE_CDR:
                mediaTypeName = "CD-R";
                break;
            case IMAPI_MEDIA_TYPE_CDRW :
                mediaTypeName = "CD-RW";
                break;
            case IMAPI_MEDIA_TYPE_DVDROM  :
                mediaTypeName = "Read-only DVD drive and/or disc";
                break;
            case IMAPI_MEDIA_TYPE_DVDRAM  :
                mediaTypeName = "DVD-RAM";
                break;
            case IMAPI_MEDIA_TYPE_DVDPLUSR  :
                mediaTypeName = "DVD+R";
                break;
            case IMAPI_MEDIA_TYPE_DVDPLUSRW:
                mediaTypeName = "DVD+RW";
                break;
            case IMAPI_MEDIA_TYPE_DVDPLUSR_DUALLAYER:
                mediaTypeName = "DVD+R Dual Layer media";
                break;
            case IMAPI_MEDIA_TYPE_DVDDASHR:
                mediaTypeName = "DVD-R";
                break;
            case IMAPI_MEDIA_TYPE_DVDDASHRW :
                mediaTypeName = "DVD-RW";
                break;
            case IMAPI_MEDIA_TYPE_DVDDASHR_DUALLAYER  :
                mediaTypeName = "DVD-R Dual Layer media";
                break;
            case IMAPI_MEDIA_TYPE_DISK  :
                mediaTypeName = "Randomly-writable, hardware-defect managed media type that reports the 'Disc' profile as current.";
                break;
            default:
                mediaTypeName = "Can't determine MediaType";
        }
        sb.append("Media Type: ").append(mediaTypeName).append(END_LINE);

        return sb.toString();
    }
}
