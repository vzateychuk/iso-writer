package ru.vez.iso.desktop.burn;

import com.ms.imapi2.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.shared.UtilsHelper;

/**
 * Manages Burn disk IMAPI operations
*/
public class BurnSrvImpl implements BurnSrv {

    private static final Logger logger = LogManager.getLogger();

    /**
     * Examines and log disc device characteristics that are independent of media inserted in the device.
     * Uses Microsoft IMAPI2 interface
     * @link https://docs.microsoft.com/en-gb/windows/win32/imapi/checking-drive-support
     * */
    @Override
    public void recorderInfo() {
        String END_LINE = System.getProperty("line.separator");

        IDiscMaster2 dm = ClassFactory.createMsftDiscMaster2();
        int count = dm.count();
        StringBuilder sb = new StringBuilder("Recorders found: ").append(count).append(END_LINE);

        // Create a DiscRecorder object for the specified burning device.
        IDiscRecorder2 recorder = ClassFactory.createMsftDiscRecorder2();

        String recorderUniqueId;
        for (int i = 0; i < count; i++) {
            recorderUniqueId = dm.item(i);

            try {
                // initialize disk recorder
                recorder.initializeDiscRecorder(recorderUniqueId);
                //  Define the new disc format and set the recorder
                IDiscFormat2Data dataWriter = ClassFactory.createMsftDiscFormat2Data();
                dataWriter.recorder(recorder);

                // print recorder info
                sb.append("--------------------------------------------------").append(END_LINE);
                sb.append( "Device Number: " + recorder.legacyDeviceNumber()).append(END_LINE);
                sb.append( "Using recorder: ").append(recorder.vendorId()).append("; ").append(recorder.productId()).append(END_LINE);
                sb.append( "Vendor Id: " + recorder.vendorId()).append(END_LINE);
                sb.append( "Product Id: " + recorder.productId()).append(END_LINE);
                sb.append( "Product Revision: " + recorder.productRevision()).append(END_LINE);
                sb.append( "VolumeName: " + recorder.volumeName()).append(END_LINE);
                sb.append( "Can Load Media: " + recorder.deviceCanLoadMedia()).append(END_LINE);
                String recorderSupport =  dataWriter.isRecorderSupported(recorder)
                        ? "Current recorder IS supported."
                        : "Current recorder IS NOT supported.";
                sb.append(recorderSupport).append(END_LINE);

                String mediaSupported = dataWriter.isCurrentMediaSupported(recorder)
                        ? "Current Media IS supported."
                        : "Current Media IS NOT supported.";
                sb.append(mediaSupported).append(END_LINE);

                // Check a few CurrentMediaStatus possibilities. Each status is associated with a bit and some combinations are legal.
                IMAPI_FORMAT2_DATA_MEDIA_STATE mediaStatusSys = dataWriter.currentMediaStatus();
                String mediaStatus;
                switch (mediaStatusSys) {
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
                sb.append("Media state: ").append(mediaStatusSys.name()).append(END_LINE);
                sb.append(mediaStatus).append(END_LINE);

                // Check a few CurrentMediaType
                IMAPI_MEDIA_PHYSICAL_TYPE mediaTypeSys = dataWriter.currentPhysicalMediaType();

                String mediaType;
                switch (mediaTypeSys) {
                    case IMAPI_MEDIA_TYPE_UNKNOWN:
                        mediaType = "Empty device or an unknown disc type.";
                        break;
                    case IMAPI_MEDIA_TYPE_CDROM:
                        mediaType = "CD-ROM";
                        break;
                    case IMAPI_MEDIA_TYPE_CDR:
                        mediaType = "CD-R";
                        break;
                    case IMAPI_MEDIA_TYPE_CDRW :
                        mediaType = "CD-RW";
                        break;
                    case IMAPI_MEDIA_TYPE_DVDROM  :
                        mediaType = "Read-only DVD drive and/or disc";
                        break;
                    case IMAPI_MEDIA_TYPE_DVDRAM  :
                        mediaType = "DVD-RAM";
                        break;
                    case IMAPI_MEDIA_TYPE_DVDPLUSR  :
                        mediaType = "DVD+R";
                        break;
                    case IMAPI_MEDIA_TYPE_DVDPLUSRW:
                        mediaType = "DVD+RW";
                        break;
                    case IMAPI_MEDIA_TYPE_DVDPLUSR_DUALLAYER:
                        mediaType = "DVD+R Dual Layer media";
                        break;
                    case IMAPI_MEDIA_TYPE_DVDDASHR:
                        mediaType = "DVD-R";
                        break;
                    case IMAPI_MEDIA_TYPE_DVDDASHRW :
                        mediaType = "DVD-RW";
                        break;
                    case IMAPI_MEDIA_TYPE_DVDDASHR_DUALLAYER  :
                        mediaType = "DVD-R Dual Layer media";
                        break;
                    case IMAPI_MEDIA_TYPE_DISK  :
                        mediaType = "Randomly-writable, hardware-defect managed media type that reports the 'Disc' profile as current.";
                        break;
                    default:
                        mediaType = "Can't determine MediaType";
                }
                sb.append("Media type: ").append(mediaTypeSys.name()).append(END_LINE);
                sb.append(mediaType).append(END_LINE);
                sb.append("----- Finished recorder: ").append(i).append(" -----").append(END_LINE);

                logger.info(sb.toString());
                if (UtilsHelper.getConfirmation(sb.toString())) {
                    recorder.closeTray();
                } else {
                    recorder.ejectMedia();
                };
            } catch (Exception e) {
                logger.error("Unable to initialize Recorder: " + recorderUniqueId, e);
            }
        }
    }
}
