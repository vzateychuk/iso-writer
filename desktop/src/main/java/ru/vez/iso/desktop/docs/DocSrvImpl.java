package ru.vez.iso.desktop.docs;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import ru.vez.iso.desktop.docs.reestr.Reestr;
import ru.vez.iso.desktop.shared.MessageSrv;
import ru.vez.iso.desktop.shared.MyConst;
import ru.vez.iso.desktop.shared.UtilsHelper;
import ru.vez.iso.desktop.state.ApplicationState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.Security;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class DocSrvImpl implements DocSrv {

    private static final Logger logger = LogManager.getLogger();

    private final ApplicationState state;
    private final Executor exec;
    private final DocMapper mapper;
    private final MessageSrv msgSrv;

    private Future<Void> future = CompletableFuture.allOf();

    public DocSrvImpl(ApplicationState appState,
                      Executor exec,
                      DocMapper mapper,
                      MessageSrv msgSrv) {
        this.state = appState;
        this.exec = exec;
        this.mapper = mapper;
        this.msgSrv = msgSrv;

        // это таинственное заклинание необходимо для выполнения проверки checksum в методе isChecksumEquals
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public void loadAsync(Path fromZipPath) {

        // Avoid multiply invocation
        if (!future.isDone()) {
            logger.debug("Async operation in progress, skipping");
            return;
        }

        logger.debug(fromZipPath);

        future = CompletableFuture.supplyAsync(() -> {

            // Clear unzipped files: delete path where unzip files will be stored
            String cachePath = state.getSettings().getIsoCachePath();
            Path unzippedPath = Paths.get(cachePath, MyConst.UNZIP_FOLDER);
            UtilsHelper.clearFolder(unzippedPath);
            UtilsHelper.unzipToFolder(fromZipPath, unzippedPath);

            // Read REESTR and save application state
            Path reestrPath = Paths.get(unzippedPath.toString(), MyConst.REESTR_FILE);
            Reestr reestr = readReestrFrom(reestrPath);
            state.setReestr(reestr);

            // REESTR Map and return list of DocumentFX
            return reestr.getDocs().stream().map(mapper::mapToDocFX).collect(Collectors.toList());

        }, exec)
            .thenAccept(state::setDocumentFXs)
            .exceptionally( (ex) -> {
                logger.error("unable to create Reestr object: " + fromZipPath, ex);
                this.msgSrv.news("Ошибка чтения DIR.zip");
                return null;
        });
    }

    @Override
    public String calculateFileHash(Path filePath, String algorithm) {

        final MessageDigest digest = DigestUtils.getDigest(algorithm);

        try ( InputStream fis = Files.newInputStream(filePath) ) {
            return Hex.encodeHexString(DigestUtils.digest(digest, fis));
        } catch (IOException ex) {
            throw new RuntimeException("Unable to calculate checksums for file: " + filePath, ex);
        }
    }

    //region PRIVATE

    /**
     * Read and deserialize a REESTR file to POJO
     * */
    private Reestr readReestrFrom(Path path) {

        try {
            BufferedReader reader = Files.newBufferedReader(path);
            String fromFile = reader.readLine();
            return new Gson().fromJson(fromFile, Reestr.class);
        } catch (Exception ex) {
            throw new RuntimeException("unable to load: " + path, ex);
        }
    }

    //endregion

}
