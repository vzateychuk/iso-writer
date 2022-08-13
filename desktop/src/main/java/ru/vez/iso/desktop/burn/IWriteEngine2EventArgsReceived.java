package ru.vez.iso.desktop.burn;

import com.ms.imapi2.IWriteEngine2EventArgs;
import com4j.Com4jObject;
import com4j.ComThread;
import com4j.EventCookie;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IWriteEngine2EventArgsReceived implements IWriteEngine2EventArgs {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public int startLba() {
        logger.debug("");
        return 0;
    }

    @Override
    public int sectorCount() {
        logger.debug("");
        return 0;
    }

    @Override
    public int lastReadLba() {
        logger.debug("");
        return 0;
    }

    @Override
    public int lastWrittenLba() {
        logger.debug("");
        return 0;
    }

    @Override
    public int totalSystemBuffer() {
        logger.debug("");
        return 0;
    }

    @Override
    public int usedSystemBuffer() {
        logger.debug("");
        return 0;
    }

    @Override
    public int freeSystemBuffer() {
        logger.debug("");
        return 0;
    }

    @Override
    public int getPtr() {
        logger.debug("");
        return 0;
    }

    @Override
    public long getPointer() {
        logger.debug("");
        return 0;
    }

    @Override
    public long getIUnknownPointer() {
        logger.debug("");
        return 0;
    }

    @Override
    public ComThread getComThread() {
        logger.debug("");
        return null;
    }

    @Override
    public void dispose() {
        logger.debug("");
    }

    @Override
    public <T extends Com4jObject> boolean is(Class<T> comInterface) {
        logger.debug("");
        return false;
    }

    @Override
    public <T extends Com4jObject> T queryInterface(Class<T> comInterface) {
        logger.debug("");
        return null;
    }

    @Override
    public <T> EventCookie advise(Class<T> eventInterface, T receiver) {
        logger.debug("");
        return null;
    }

    @Override
    public void setName(String name) {
        logger.debug("name: " + name);
    }
}
