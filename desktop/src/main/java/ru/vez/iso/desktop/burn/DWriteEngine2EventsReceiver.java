package ru.vez.iso.desktop.burn;

import com.ms.imapi2.DWriteEngine2Events;
import com4j.Com4jObject;
import com4j.ComThread;
import com4j.DISPID;
import com4j.EventCookie;

public class DWriteEngine2EventsReceiver implements DWriteEngine2Events {

    @DISPID(7)
    @Override
    public void update(Com4jObject object, Com4jObject progress) {
        System.out.println("DWriteEngine2EventsReceiver.update");
    }

    @Override
    public int getPtr() {
        System.out.println("DWriteEngine2EventsReceiver.getPtr()");
        return 0;
    }

    @Override
    public long getPointer() {
        System.out.println("DWriteEngine2EventsReceiver.getPointer()");
        return 0;
    }

    @Override
    public long getIUnknownPointer() {
        System.out.println("DWriteEngine2EventsReceiver.getIUnknownPointer()");
        return 0;
    }

    @Override
    public ComThread getComThread() {
        System.out.println("DWriteEngine2EventsReceiver.getComThread()");
        return (ComThread)Thread.currentThread();
    }

    @Override
    public void dispose() {
        System.out.println("DWriteEngine2EventsReceiver.dispose()");
    }

    @Override
    public <T extends Com4jObject> boolean is(Class<T> comInterface) {
        System.out.println("DWriteEngine2EventsReceiver.is()");
        return false;
    }

    @Override
    public <T extends Com4jObject> T queryInterface(Class<T> comInterface) {
        System.out.println("DWriteEngine2EventsReceiver.queryInterface()");
        return null;
    }

    @Override
    public <T> EventCookie advise(Class<T> eventInterface, T receiver) {
        return () -> System.out.println(this.getClass().getName() + ".close");
    }

    @Override
    public void setName(String name) {
        System.out.println("DWriteEngine2EventsReceiver.setName(): " + name);
    }
}
