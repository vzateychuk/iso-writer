package ru.vez.iso.desktop.burn;

import com.ms.imapi2.DDiscFormat2DataEvents;
import com4j.Com4jObject;
import com4j.ComThread;
import com4j.DISPID;
import com4j.EventCookie;

public class DDiscFormat2DataEventsReceiver implements DDiscFormat2DataEvents {

    @DISPID(7)
    @Override
    public void update(Com4jObject object, Com4jObject progress) {
        System.out.println(this.getClass().getName() + ".update");
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
        return null;
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

    }
}
