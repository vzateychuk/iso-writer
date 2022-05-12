package ru.vez.iso.desktop.docs;

import com.google.gson.Gson;
import javafx.collections.ObservableMap;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import ru.vez.iso.desktop.docs.reestr.Reestr;
import ru.vez.iso.desktop.shared.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.Security;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class DocumentSrvImpl implements DocumentSrv {

    private static final Logger logger = LogManager.getLogger();

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final Executor exec;
    private final DocumentMapper mapper;
    private final MessageSrv msgSrv;

    private Future<Void> future = CompletableFuture.allOf();

    public DocumentSrvImpl(ObservableMap<AppStateType,
                           AppStateData> appState,
                           Executor exec,
                           DocumentMapper mapper,
                           MessageSrv msgSrv) {
        this.appState = appState;
        this.exec = exec;
        this.mapper = mapper;
        this.msgSrv = msgSrv;

        // это таинственное заклинание необходимо для выполнения проверки checksum в методе isChecksumEquals
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public void loadAsync(Path dirZip) {

        // Avoid multiply invocation
        if (!future.isDone()) {
            logger.debug("Async operation in progress, skipping");
            return;
        }

        logger.debug(dirZip);

        future = CompletableFuture.supplyAsync(() -> {

            // Clear unzipped files: delete path where unzip files will be stored
            String cachePath = ((AppStateData<AppSettings>)appState.get(AppStateType.SETTINGS)).getValue().getIsoCachePath();
            Path unzippedPath = Paths.get(cachePath, MyConst.UNZIP_FOLDER);
            UtilsHelper.clearFolder(unzippedPath);
            UtilsHelper.unzipToFolder(unzippedPath, dirZip);

            // Read REESTR and save application state
            Path reestrPath = Paths.get(unzippedPath.toString(), MyConst.REESTR_FILE);
            Reestr reestr = readReestrFrom(reestrPath);
            appState.put(AppStateType.REESTR, AppStateData.<Reestr>builder().value(reestr).build());

            // REESTR Map and return list of DocumentFX
            return reestr.getDocs().stream().map(mapper::mapToDocFX).collect(Collectors.toList());

        }, exec).thenAccept(docs ->
                appState.put(AppStateType.DOCUMENTS, AppStateData.<List<DocumentFX>>builder().value(docs).build())
        ).exceptionally((ex) -> {
            logger.error("unable to create Reestr object: " + dirZip, ex);
            this.msgSrv.news("Ошибка чтения DIR.zip");
            return null;
        } );
    }

    @Override
    public boolean compareCheckSum(Path checksumFile, Path dirZip) {

        final MessageDigest gostDigest = DigestUtils.getDigest(MyConst.ALGO_GOST);

        try ( InputStream dirZipFis = Files.newInputStream(dirZip) ) {
            String expectedHash = new String(Files.readAllBytes(checksumFile), StandardCharsets.UTF_8);
            String actualHash = Hex.encodeHexString(DigestUtils.digest(gostDigest, dirZipFis));
            logger.debug("Compare Hash\nexpect:\t'{}'\nactual:\t'{}'", expectedHash, actualHash);
            return actualHash.equals(expectedHash);
        } catch (Exception ex) {
            logger.error("Unable to compare checksums for: {}, {}", checksumFile, dirZip, ex);
            throw new RuntimeException(ex);
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
