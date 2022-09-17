package ru.vez.iso.desktop.docs;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import ru.vez.iso.desktop.docs.reestr.Reestr;
import ru.vez.iso.desktop.exceptions.FileCacheException;
import ru.vez.iso.desktop.shared.MyConst;
import ru.vez.iso.desktop.shared.UtilsHelper;
import ru.vez.iso.desktop.state.ApplicationState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.Security;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class DocSrvImpl implements DocSrv {

    private static final Logger logger = LogManager.getLogger();

    private final ApplicationState state;
    private final DocMapper mapper;

    private Future<Void> future = CompletableFuture.allOf();

    public DocSrvImpl(ApplicationState appState, DocMapper mapper) {
        this.state = appState;
        this.mapper = mapper;

        // это таинственное заклинание необходимо для выполнения проверки checksum в методе isChecksumEquals
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public List<DocumentFX> loadREESTR(Path zipFile) {

        // Clear unzipped files: delete path where unzip files will be stored
        String cachePath = state.getSettings().getIsoCachePath();
        Path unzipDir = Paths.get(cachePath, MyConst.UNZIP_FOLDER);
        UtilsHelper.clearFolder(unzipDir);
        UtilsHelper.unzipToFolder(zipFile, unzipDir);

        // Read REESTR and save application state
        Path reestrPath = Paths.get(unzipDir.toString(), MyConst.REESTR_FILE);
        Reestr reestr = readREESTRFile(reestrPath);
        state.setReestr(reestr);

        // REESTR Map and return list of DocumentFX
        return reestr.getDocs().stream().map(mapper::mapToDocFX).collect(Collectors.toList());
    }

    @Override
    public String calculateFileHash(Path filePath, String algorithm) {

        final MessageDigest digest = DigestUtils.getDigest(algorithm);

        try ( InputStream fis = Files.newInputStream(filePath) ) {
            return Hex.encodeHexString(DigestUtils.digest(digest, fis));
        } catch (IOException ex) {
            throw new FileCacheException("Unable to calculate checksums for file: " + filePath, ex);
        }
    }

    @Override
    public String readFile(Path filePath) {
        try {
            return new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FileCacheException("Unable to read checksums from file: " + filePath, e);
        }
    }

    //region PRIVATE

    /**
     * Read and deserialize a REESTR file to POJO
     * */
    private Reestr readREESTRFile(Path path) {

        try( BufferedReader reader = Files.newBufferedReader(path) ) {
            String fromFile = reader.readLine();
            return new Gson().fromJson(fromFile, Reestr.class);
        } catch (Exception ex) {
            throw new FileCacheException("unable to load: " + path, ex);
        }
    }

    //endregion

}
