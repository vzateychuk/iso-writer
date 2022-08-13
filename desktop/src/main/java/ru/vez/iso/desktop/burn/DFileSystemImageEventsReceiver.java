package ru.vez.iso.desktop.burn;

import com.ms.imapi2.DFileSystemImageEvents;
import com4j.Com4jObject;
import com4j.ComThread;
import com4j.DISPID;
import com4j.EventCookie;

public class DFileSystemImageEventsReceiver implements DFileSystemImageEvents {

    private String name;

    @DISPID(7)
    @Override
    public void update(Com4jObject object, String currentFile, int copiedSectors, int totalSectors) {
        System.out.println("DFileSystemImageEventsReceiver.update");
    }

    @Override
    public int getPtr() {
        return 0;
    }

    @Override
    public long getPointer() {
        return 0;
    }

    @Override
    public long getIUnknownPointer() {
        return 0;
    }

    @Override
    public ComThread getComThread() {
        return (ComThread)Thread.currentThread();
    }

    @Override
    public void dispose() {

    }

    @Override
    public <T extends Com4jObject> boolean is(Class<T> comInterface) {
        return false;
    }

    @Override
    public <T extends Com4jObject> T queryInterface(Class<T> comInterface) {
        return null;
    }

    @Override
    public <T> EventCookie advise(Class<T> eventInterface, T receiver) {
        return () -> System.out.println(this.getClass().getName() + ".close");
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

}
