package ru.vez.iso.desktop.burn.tools;

import com.ms.imapi2.*;

/**
 * Burning a Disc Image
 * Mastering (burning a disc) using IMAPI consists of the following steps:
 * 1. Construct a file system image that contains the directories and files to write disc.
 * 2. Set up a disc recorder to communicate with the optical device.
 * 3. Create a data writer and burn the image to disc.
 *
 * 1. Construct a burn image
 * https://docs.microsoft.com/en-gb/windows/win32/imapi/burning-a-disc#construct-a-burn-image
 * A burn image is a data stream that is ready to be written to optical media. The burn image for ISO9660, Joliet and UDF formats consists of a file system of individual files and directories. The CFileSystemImage object is the file system object that holds the files and directories to place on the optical media. The IFileSystemImage interface provides access to the file system object and settings.
 * After creating the file system object, call the IFileSystemImage::CreateFileItem and IFileSystemImage::CreateDirectoryItem methods to create the file and directory objects, respectively. The file and directory objects can be used to provide specific details about the file and directory. The event handler methods available for IFileSystemImage can identify the current file being added to the file system image, the number of sectors already copied, and the total number of sectors to be copied.
 * Finally, call IFileSystemImage::CreateResultImage to create a data stream and provides access through IFileSystemImageResult. The new data stream can then be provided directly to the IDiscFormat2Data::Write method or be saved to a file for later use.
 *
 * 2. Set up a disc recorder
 * https://docs.microsoft.com/en-gb/windows/win32/imapi/burning-a-disc#set-up-a-disc-recorder
 * The MsftDiscMaster2 object provides an enumeration of the optical devices on the system. The IDiscMaster2 interface provides access to the resultant device enumeration. Traverse the enumerations to locate an appropriate recording device. The MsftDiscMaster2 object also provides event notifications when optical devices are added to or deleted from a computer.
 * After finding an optical recorder and retrieving its ID, create an MsftDiscRecorder2 object and initialize the recorder using the device ID. The IDiscRecorder2 interface provides access to the recorder object as well as some basic device information such as vendor ID, product ID, product revision, and methods to eject the media and close the tray.
 *
 * 3. Create a data writer and write the burn image
 * https://docs.microsoft.com/en-gb/windows/win32/imapi/burning-a-disc#create-a-data-writer-and-write-the-burn-image
 * The MsftDiscFormat2Data object provides the writing method, the properties about the write function and media-specific properties. The IDiscFormat2Data interface provides access to the MsftDiscFormat2Data object.
 * The disc recorder links to the format writer using the IDiscFormat2Data::put_Recorder property. After the recorder is bound to the format writer, you can perform queries regarding the media and update write-specific properties before writing the result image to disc using the IDiscFormat2Data::Write method.
 * Other format writing interfaces provided by IMAPI work similarly; additional format writing interfaces include:
 * - IDiscFormat2Erase erases rewritable optical media.
 * - IDiscFormat2RawCD writes a raw image to optical media.
 * - IDiscFormat2TrackAtOnce writes audio tracks to optical media.
 *  Note: Preventing Logoff or Suspend During a Burn. It is possible for a power state transition to take place during a burn operation (i.e. user log-off or system suspend) which leads to the interruption of the burn process and possible data loss. For programming considerations, see Preventing Logoff or Suspend During a Burn.
 *
 * The code contains no error checking, and assumes the following:
 *
 * A compatible disc device is installed on the system.
 * The disc device is the first drive on the system.
 * A compatible disc is inserted in the disc device.
 * The disc is blank.
 * Files to write to disc are located in "g:\burndir".
 *
 * @link https://docs.microsoft.com/en-gb/windows/win32/imapi/burning-a-disc
 * */
public class BurnDisk {

    // *** CD/DVD disc file system types
    public static void main(String[] args) {

        IDiscMaster2 discMaster = ClassFactory.createMsftDiscMaster2();
        int count = discMaster.count();

        //
        if (count > 1) {
            throw new IllegalStateException("Expected only one recorder, got: " + count);
        }
        // Expected the disc device is the first drive on the system.
        String recorderUniqueId = discMaster.item(0);

        // Create a DiscRecorder object for the specified burning device.
        IDiscRecorder2 recorder = ClassFactory.createMsftDiscRecorder2();
        recorder.initializeDiscRecorder(recorderUniqueId);
        System.out.println("Using recorder: " + recorder.vendorId() + " " + recorder.productId());

        // Create a new file system image and retrieve root directory
        IFileSystemImage3 fileSystemImage = ClassFactory.createMsftFileSystemImage();
        // fileSystemImage.root("...\\burndir");
        IFsiDirectoryItem directoryItem = fileSystemImage.root();

        // Create the new disc format and set the recorder
        IDiscFormat2Data dataWriter = ClassFactory.createMsftDiscFormat2Data();
        dataWriter.recorder(recorder);
        dataWriter.clientName("IMAPIv2 TEST");

        fileSystemImage.chooseImageDefaults(recorder);

        // Add the directory and its contents to the file system
        directoryItem.addTree("./burndir", false);

        // Create an image from the file system
        IFileSystemImageResult fileSystemImageResult = fileSystemImage.createResultImage();
        IStream stream = fileSystemImageResult.imageStream();

        // Write stream to disc using the specified recorder.
        System.out.println("Writing content to disc...");
        dataWriter.write(stream);
        System.out.println("----- Finished writing content -----");
    }
}
