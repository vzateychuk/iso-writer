package ru.vez.iso.desktop.main.burner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.shared.UtilsHelper;

import java.util.concurrent.Future;

public class BurnerNoop implements Burner {

    private static final Logger logger = LogManager.getLogger();

    private Future<Void> future;

    @Override
    public void startBurn(String objectId) {

        logger.debug("StorageUnitID: {}", objectId);

        // Avoid multiply invocation
        if (!future.isDone()) {
            logger.warn("Async operation in progress, skipping");
            return;
        }

        UtilsHelper.makeDelaySec(3);

        throw new IllegalStateException("Unable to burn: " + objectId);
    }
}
