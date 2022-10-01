package ru.vez.iso.desktop.burn;

import com.ms.imapi2.*;
import lombok.Builder;
import lombok.Data;

import java.util.Optional;

import static com.ms.imapi2.IMAPI_FORMAT2_DATA_MEDIA_STATE.*;
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
    private boolean isMediaProtected;   // if Media is write protected
    private Integer nextWritableAddress;        // 0 value means the disc is empty

    private IMAPI_FORMAT2_DATA_MEDIA_STATE mediaState; // The state (usability) of the current media
    private IMAPI_MEDIA_PHYSICAL_TYPE mediaType;    // Physical type of the optical media loaded

    public boolean isReady(MediaType expectedType) {

        return this.isRecorderSupported && this.isCurrentMediaSupported && !isMediaProtected
                && expectedType.getPhysicalTypesAllowed().contains(this.mediaType)
                && Optional.ofNullable(nextWritableAddress).orElse(-1) == 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Recorder: ").append(vendorId).append(SP).append(productId).append(END_LINE)
                .append("Device number: ").append(legacyDeviceNumber).append(END_LINE)
                .append( "Vendor Id: ").append(vendorId).append(END_LINE)
                .append( "Product Id: ").append(productId).append(END_LINE)
                .append( "Product Revision: ").append(productRevision).append(END_LINE)
                .append( "VolumeName: ").append(volumeName).append(END_LINE)
                .append( "Can Load Media: ").append(deviceCanLoadMedia).append(END_LINE)
        .append(isRecorderSupported ? "Recorder IS supported." : "Recorder NOT supported.").append(END_LINE)
        .append(isCurrentMediaSupported ? "Media IS supported." : "Media NOT supported.").append(END_LINE);

        // Check a few MediaState possibilities. Each status is associated with a bit and some combinations are legal.
        mediaState = Optional.ofNullable(mediaState).orElse(IMAPI_FORMAT2_DATA_MEDIA_STATE_UNKNOWN);

        isMediaProtected = (mediaState.comEnumValue() & IMAPI_FORMAT2_DATA_MEDIA_STATE_WRITE_PROTECTED.comEnumValue()) != 0;
        sb.append("Media Protected: ").append(isMediaProtected).append(END_LINE);

        // Check a few CurrentMediaType
        this.mediaType = Optional.ofNullable(this.mediaType).orElse( IMAPI_MEDIA_TYPE_UNKNOWN );
        Optional.ofNullable(nextWritableAddress).ifPresent(address ->
            sb.append("Media is ").append(address == 0 ? " empty" : "NOT empty").append(END_LINE)
        );
        sb.append("Media Type: ").append(mediaType.name()).append(" (").append(mediaType.getDesc()).append(")").append(END_LINE);
        sb.append("Media State: ").append(mediaState.name()).append(" (").append(mediaState.getDesc()).append(")").append(END_LINE);

        return sb.toString();
    }
}
