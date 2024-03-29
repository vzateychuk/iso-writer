package ru.vez.iso.desktop.burn;

import com.ms.imapi2.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.ms.imapi2.IMAPI_FORMAT2_DATA_MEDIA_STATE.IMAPI_FORMAT2_DATA_MEDIA_STATE_RANDOMLY_WRITABLE;
import static com.ms.imapi2.IMAPI_FORMAT2_DATA_MEDIA_STATE.IMAPI_FORMAT2_DATA_MEDIA_STATE_UNKNOWN;
import static com.ms.imapi2.IMAPI_MEDIA_PHYSICAL_TYPE.IMAPI_MEDIA_TYPE_UNKNOWN;

/**
 * Manages Burn disk IMAPI operations
*/
public class BurnSrvImpl implements BurnSrv {

    private static final Logger logger = LogManager.getLogger();
    // see https://stackoverflow.com/questions/5616986/how-to-retrieve-and-set-burn-speed-using-imapi2
    private static final int IMAPI_SECTORS_PER_SECOND_AT_1X_DVD = 680;
    public static final String IMAP_IV_2 = "IMAPIv2";

    private IDiscMaster2 dm;

    public BurnSrvImpl() {
        dm = ClassFactory.createMsftDiscMaster2();
    }

    /**
     * Examines and log disc device characteristics that are independent of media inserted in the device.
     * Uses Microsoft IMAPI2 interface
     * @link https://docs.microsoft.com/en-gb/windows/win32/imapi/checking-drive-support
     * */
    public List<RecorderInfo> getRecordersInfo() {

        IDiscMaster2 dm = ClassFactory.createMsftDiscMaster2();
        int count = dm.count();
        logger.info("found recorder: " + count);

        List<RecorderInfo> infos = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            RecorderInfo recorderInfo = this.recorderInfo(i);
            infos.add(recorderInfo);
        }

        return infos;
    }

    @Override
    public RecorderInfo recorderInfo(int recorderIndex) {

        IDiscRecorder2 recorder = ClassFactory.createMsftDiscRecorder2();
        String recorderUniqueId = dm.item(0);

        // initialize disk recorder
        recorder.initializeDiscRecorder(recorderUniqueId);
        //  Define the new disc format and set the recorder
        IDiscFormat2Data dataWriter = ClassFactory.createMsftDiscFormat2Data();
        dataWriter.recorder(recorder);
        dataWriter.clientName(IMAP_IV_2);

        logger.info("Using recorder: {} {}", recorder.vendorId(), recorder.productId());

        IMAPI_FORMAT2_DATA_MEDIA_STATE mediaState = IMAPI_FORMAT2_DATA_MEDIA_STATE_UNKNOWN;
        IMAPI_MEDIA_PHYSICAL_TYPE mediaType = IMAPI_MEDIA_TYPE_UNKNOWN;
        Integer nextWritableAddress = null;
        if (dataWriter.isRecorderSupported(recorder) && dataWriter.isCurrentMediaSupported(recorder)) {
            mediaState = dataWriter.currentMediaStatus();
            mediaType = dataWriter.currentPhysicalMediaType();

            //Check if disc is empty
            IDiscFormat2Data discData = ClassFactory.createMsftDiscFormat2Data();
            discData.recorder(recorder);
            discData.clientName(IDiscMaster2.class.getSimpleName());
            nextWritableAddress = discData.nextWritableAddress();
        }


        // Build recorder info
        return RecorderInfo.builder()
                .legacyDeviceNumber(recorder.legacyDeviceNumber())
                .vendorId(recorder.vendorId())
                .productId(recorder.productId())
                .productRevision(recorder.productRevision())
                .volumeName(recorder.volumeName())
                .deviceCanLoadMedia(recorder.deviceCanLoadMedia())
                .isRecorderSupported(dataWriter.isRecorderSupported(recorder))
                .isCurrentMediaSupported(dataWriter.isCurrentMediaSupported(recorder))
                .nextWritableAddress(nextWritableAddress)
                .mediaState(mediaState)
                .mediaType(mediaType)
                .build();
    }

    /**
     * Burning file image on burner #recorderIndex
     * */
    @Override
    public void burn(int recorderIndex, int burnSpeed, Path isoDir, String discTitle) {

        logger.debug("recorder: {}, burnSpeed: {}, dir: {}, diskTitle: {}", recorderIndex, burnSpeed, isoDir, discTitle);
        IDiscRecorder2 recorder = ClassFactory.createMsftDiscRecorder2();
        String recorderUniqueId = dm.item(0);

        // initialize disk recorder
        recorder.initializeDiscRecorder(recorderUniqueId);
        //  Define the new disc format and set the recorder
        IDiscFormat2Data dataWriter = ClassFactory.createMsftDiscFormat2Data();
        dataWriter.recorder(recorder);
        dataWriter.clientName(IMAP_IV_2);
        logger.info("Using recorder: {} {}", recorder.vendorId(), recorder.productId());

        // Validate recorder/media status
        if (!dataWriter.isRecorderSupported(recorder)) {
            logger.error("Recorder is not supported");
            throw new IllegalStateException("Recorder is not supported");
        }
        if (!dataWriter.isCurrentMediaSupported(recorder)) {
            logger.error("CurrentMedia is not supported");
            throw new IllegalStateException("CurrentMedia is not supported");
        }

        // Check if Media is write protected / not empty
        IDiscFormat2Data discData = ClassFactory.createMsftDiscFormat2Data();
        discData.recorder(recorder);
        discData.clientName(IMAP_IV_2);
        IMAPI_FORMAT2_DATA_MEDIA_STATE mediaStatus = discData.currentMediaStatus();
        logger.debug("Media status: {}", mediaStatus);
        if ((mediaStatus.comEnumValue() & IMAPI_FORMAT2_DATA_MEDIA_STATE.IMAPI_FORMAT2_DATA_MEDIA_STATE_WRITE_PROTECTED.comEnumValue()) != 0) {
            logger.error("Media is write protected / not empty.");
            throw new IllegalStateException("Media is write protected / not empty.");
        }
        if ( mediaStatus == IMAPI_FORMAT2_DATA_MEDIA_STATE_RANDOMLY_WRITABLE) {
            discData.forceOverwrite(true);
        }
        discData.forceMediaToBeClosed(true);
        discData.advise(DDiscFormat2DataEvents.class, new DDiscFormat2DataEventsReceiver());

        //Check if disc is empty
        int addr = discData.nextWritableAddress();
        if (addr != 0) {
            logger.error("Media is write protected / not empty.");
            throw new IllegalStateException("Disc is not empty, not writing.");
        }

        logger.debug("Preparing burn fileSystemImage, forceOverwrite: {}...", discData.forceOverwrite());
        // Create a new file system image and retrieve root directory
        IFileSystemImage3 fileSystemImage = ClassFactory.createMsftFileSystemImage();
        IFsiDirectoryItem directoryItem = fileSystemImage.root();

        // Create the new disc format and set the recorder
        fileSystemImage.chooseImageDefaults(recorder);
        // fileSystemImage.volumeName(discTitle);
        fileSystemImage.volumeName(discTitle);
        fileSystemImage.advise(DFileSystemImageEvents.class, new DFileSystemImageEventsReceiver());

        // Add the contents to the file system
        directoryItem.addTree(isoDir.toString(), false);

        // Create an image from the file system
        IFileSystemImageResult fileSystemImageResult = fileSystemImage.createResultImage();
        // fileSystemImageResult.setName("IMAPIv2");
        IStream stream = fileSystemImageResult.imageStream();

        // https://stackoverflow.com/questions/5616986/how-to-retrieve-and-set-burn-speed-using-imapi2/55039084#55039084
        // Write spped: dataWriter.setWriteSpeed(sectorsPerSecSpeed, false);

        // Write stream to disc using the specified recorder.
        logger.debug("Start writing content to disc with speed: {} ...", dataWriter.currentWriteSpeed());
        dataWriter.write(stream);
        logger.debug("Finished writing content, open tray...");
    }

    /**
     * Open recorder tray
     * */
    @Override
    public void openTray(int recorderIndex) {

        IDiscRecorder2 recorder = ClassFactory.createMsftDiscRecorder2();
        String recorderUniqueId = dm.item(0);
        // initialize disk recorder
        recorder.initializeDiscRecorder(recorderUniqueId);
        logger.info("Using recorder: {} {}", recorder.vendorId(), recorder.productId());

        // Create a DiscRecorder object for the specified burning device.
        recorder.ejectMedia();
    }
}
