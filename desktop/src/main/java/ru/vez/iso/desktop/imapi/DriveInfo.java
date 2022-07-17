package ru.vez.iso.desktop.imapi;

import com.ms.imapi2.ClassFactory;
import com.ms.imapi2.IDiscMaster2;
import com.ms.imapi2.IDiscRecorder2;

/**
 * Checking Drive Support
* The following example examines disc device characteristics that are independent of media inserted in the device.
 * More specifically, it retrieves lists of supported features, supported profiles, and supported mode pages,
 * as well as the current feature settings and profile.
 * @link https://docs.microsoft.com/en-gb/windows/win32/imapi/checking-drive-support
* */
public class DriveInfo {

    public static void main(String[] args) {
       IDiscMaster2 dm = ClassFactory.createMsftDiscMaster2();
       int count = dm.count();
       System.out.println("Recorders found: : " + count);

        //Pick the first recorder on the system
        String recorderUniqueId = null;
        for (int i = 0; i < count; i++) {
            recorderUniqueId = dm.item(i);
            System.out.println(i + ". recorderUniqueId: " + recorderUniqueId);
        }

        IDiscRecorder2 recorder = ClassFactory.createMsftDiscRecorder2();
        try {
            recorder.initializeDiscRecorder(recorderUniqueId);
        } catch (Exception e) {
            System.out.println("Unable to initialize DiscRecorder");
            e.printStackTrace();
            return;
        }
        System.out.println("Using recorder: " + recorder.vendorId() + "; " + recorder.productId());

        System.out.println("*** - Formatting to display recorder info - ***\n--------------------------------------------------");
        System.out.println( " ActiveRecorderId: " + recorder.activeDiscRecorder());
        System.out.println( "        Vendor Id: " + recorder.vendorId());
        System.out.println( "       Product Id: " + recorder.productId());
        System.out.println( " Product Revision: " + recorder.productRevision());
        System.out.println( "       VolumeName: " + recorder.volumeName());
        System.out.println( "   Can Load Media: " + recorder.deviceCanLoadMedia());
        System.out.println( "    Device Number: " + recorder.legacyDeviceNumber());

        System.out.println("----- Finished content -----");
    }

}
