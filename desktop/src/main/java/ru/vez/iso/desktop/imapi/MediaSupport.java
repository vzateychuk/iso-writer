package ru.vez.iso.desktop.imapi;

import com.ms.imapi2.*;

/**
 * Checking Media Support
 * The following script examines characteristics of the media loaded in the disc device. More specifically, it checks the media type and the current state of the media, as well as recorder and media compatibility. This script can be adapted to verify compatibility and state issues before using the media.
 * @link https://docs.microsoft.com/en-gb/windows/win32/imapi/checking-media-support?redirectedfrom=MSDN
 * */
public class MediaSupport {

    public static void main(String[] args) {

        IDiscMaster2 discMaster = ClassFactory.createMsftDiscMaster2();
        int count = discMaster.count();

//Pick the first recorder on the system
        String recorderUniqueId = null;
        for (int i = 0; i < count; i++)
        {
            String cur = discMaster.item(i);
            recorderUniqueId = cur;
        }

        // Create a DiscRecorder object for the specified burning device.
        IDiscRecorder2 recorder = ClassFactory.createMsftDiscRecorder2();
        recorder.initializeDiscRecorder(recorderUniqueId);
        System.out.println("Using recorder: " + recorder.vendorId() + " " + recorder.productId());

        //  Define the new disc format and set the recorder
        IDiscFormat2Data dataWriter = ClassFactory.createMsftDiscFormat2Data();
        dataWriter.recorder(recorder);

        boolean isRecorderSupported = dataWriter.isRecorderSupported(recorder);
        if (isRecorderSupported) {
            System.out.println("--- Current recorder IS supported. ---");
        } else {
            System.out.println("--- Current recorder IS NOT supported. ---");
        }

        boolean isMediaSupported = dataWriter.isCurrentMediaSupported(recorder);
        if (isMediaSupported) {
            System.out.println("--- Current media IS supported. ---");
        } else {
            System.out.println("--- Current media IS NOT supported. ---");
        }

        // Check a few CurrentMediaStatus possibilities. Each status is associated with a bit and some combinations are legal.
        IMAPI_FORMAT2_DATA_MEDIA_STATE curMediaStatus = dataWriter.currentMediaStatus();
        System.out.println( "--- Checking CurrentMedia: " + curMediaStatus.name() + " ---");

        String mediaStatus;
        switch (curMediaStatus) {
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
        System.out.println( mediaStatus);

        // Check a few CurrentMediaType
        IMAPI_MEDIA_PHYSICAL_TYPE mediaType = dataWriter.currentPhysicalMediaType();
        System.out.println( "--- Checking MediaType: " + mediaType.name() + " ---");

        String mediaTypeString;
        switch (mediaType) {
            case IMAPI_MEDIA_TYPE_UNKNOWN:
                mediaTypeString = "Empty device or an unknown disc type.";
                break;
            case IMAPI_MEDIA_TYPE_CDROM:
                mediaTypeString = "CD-ROM";
                break;
            case IMAPI_MEDIA_TYPE_CDR:
                mediaTypeString = "CD-R";
                break;
            case IMAPI_MEDIA_TYPE_CDRW :
                mediaTypeString = "CD-RW";
                break;
            case IMAPI_MEDIA_TYPE_DVDROM  :
                mediaTypeString = "Read-only DVD drive and/or disc";
                break;
            case IMAPI_MEDIA_TYPE_DVDRAM  :
                mediaTypeString = "DVD-RAM";
                break;
            case IMAPI_MEDIA_TYPE_DVDPLUSR  :
                mediaTypeString = "DVD+R";
                break;
            case IMAPI_MEDIA_TYPE_DVDPLUSRW:
                mediaTypeString = "DVD+RW";
                break;
            case IMAPI_MEDIA_TYPE_DVDPLUSR_DUALLAYER:
                mediaTypeString = "DVD+R Dual Layer media";
                break;
            case IMAPI_MEDIA_TYPE_DVDDASHR:
                mediaTypeString = "DVD-R";
                break;
            case IMAPI_MEDIA_TYPE_DVDDASHRW :
                mediaTypeString = "DVD-RW";
                break;
            case IMAPI_MEDIA_TYPE_DVDDASHR_DUALLAYER  :
                mediaTypeString = "DVD-R Dual Layer media";
                break;
            case IMAPI_MEDIA_TYPE_DISK  :
                mediaTypeString = "Randomly-writable, hardware-defect managed media type that reports the 'Disc' profile as current.";
                break;
            default:
                mediaTypeString = "Can't determine MediaType";
        }
        System.out.println(mediaTypeString);

    }
}
