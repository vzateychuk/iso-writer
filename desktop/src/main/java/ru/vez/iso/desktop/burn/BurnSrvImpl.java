package ru.vez.iso.desktop.burn;

import com.ms.imapi2.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages Burn disk IMAPI operations
*/
public class BurnSrvImpl implements BurnSrv {

    private static final Logger logger = LogManager.getLogger();

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

        // Create a DiscRecorder object for the specified burning device.
        IDiscRecorder2 recorder = ClassFactory.createMsftDiscRecorder2();
        String recorderUniqueId = dm.item(recorderIndex);

        // initialize disk recorder
        recorder.initializeDiscRecorder(recorderUniqueId);
        //  Define the new disc format and set the recorder
        IDiscFormat2Data dataWriter = ClassFactory.createMsftDiscFormat2Data();
        dataWriter.recorder(recorder);

        IMAPI_FORMAT2_DATA_MEDIA_STATE mediaState = null;
        IMAPI_MEDIA_PHYSICAL_TYPE mediaType = null;
        if (dataWriter.isRecorderSupported(recorder) && dataWriter.isCurrentMediaSupported(recorder)) {
            mediaState = dataWriter.currentMediaStatus();
            mediaType = dataWriter.currentPhysicalMediaType();
        }

        // Build recorder info
        RecorderInfo info = RecorderInfo.builder()
                .legacyDeviceNumber(recorder.legacyDeviceNumber())
                .vendorId(recorder.vendorId())
                .productId(recorder.productId())
                .productRevision(recorder.productRevision())
                .volumeName(recorder.volumeName())
                .deviceCanLoadMedia(recorder.deviceCanLoadMedia())
                .isRecorderSupported(dataWriter.isRecorderSupported(recorder))
                .isCurrentMediaSupported(dataWriter.isCurrentMediaSupported(recorder))
                .mediaState(mediaState)
                .mediaType(mediaType)
                .build();
        return info;
    }

    /**
     * Burning file image on burner #recorderIndex
     * */
    @Override
    public void burn(int recorderIndex, Path filepath) {

        logger.info("recorder: {}: file: {}", recorderIndex, filepath);

        // Create a DiscRecorder object for the specified burning device.
        IDiscRecorder2 recorder = ClassFactory.createMsftDiscRecorder2();
        String recorderUniqueId = this.dm.item(recorderIndex);
        recorder.initializeDiscRecorder(recorderUniqueId);

        //  Define the new disc format and set the recorder
        IDiscFormat2Data dataWriter = ClassFactory.createMsftDiscFormat2Data();
        dataWriter.recorder(recorder);
        dataWriter.clientName("IMAPIv2");

        if (!dataWriter.isRecorderSupported(recorder)) {
            logger.warn("Recorder is not supported");
            throw new IllegalStateException("Recorder is not supported");
        }
        if (!dataWriter.isCurrentMediaSupported(recorder)) {
            logger.warn("CurrentMedia is not supported");
            throw new IllegalStateException("CurrentMedia is not supported");
        }

        logger.debug("Preparing burn fileSystemImage...");

        // Create a new file system image and retrieve root directory
        IFileSystemImage3 fileSystemImage = ClassFactory.createMsftFileSystemImage();
        // fileSystemImage.root("C:\\Users\\vez\\tmp\\burndir");
        IFsiDirectoryItem directoryItem = fileSystemImage.root();

        // Create the new disc format and set the recorder
        fileSystemImage.chooseImageDefaults(recorder);

        // Add the contents to the file system
        directoryItem.addTree(filepath.toString(), false);

        // Create an image from the file system
        IFileSystemImageResult fileSystemImageResult = fileSystemImage.createResultImage();
        IStream stream = fileSystemImageResult.imageStream();

        // Write stream to disc using the specified recorder.
        logger.debug("Start writing content to disc...");
        dataWriter.write(stream);
        logger.debug("Finished writing content...");
    }
}
