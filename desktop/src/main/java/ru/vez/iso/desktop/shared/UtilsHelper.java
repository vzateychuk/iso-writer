package ru.vez.iso.desktop.shared;

import com.github.stephenc.javaisotools.loopfs.iso9660.Iso9660FileEntry;
import com.github.stephenc.javaisotools.loopfs.iso9660.Iso9660FileSystem;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.exceptions.FileCacheException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class UtilsHelper {

    private static final Logger logger = LogManager.getLogger();

    private UtilsHelper() {}

    /**
     * Delay current thread on 'delay' sec
     *
     * @param delay sec
     * */
    public static void makeDelaySec(int delay) {
        try {
            Thread.sleep(delay * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Parse text value to int. Default value will be used if parse fails
     *
     * @param text - value will be parsed to Int
     * @param defaultVal - value will be parsed if the text fails to parse
     * @return parsed value
     * */
    public static int parseIntOrDefault(String text, String defaultVal) {

        try{
            int val = Integer.parseInt(text);
            if (val > 1 && val < 10_000) {
                return val;
            } else {
                logger.warn("Incorrect value {}, default {} will be used", text, defaultVal);
            }
        } catch (NumberFormatException ex){
            logger.warn("unable to convert to int, value: {}", text);
        }
        return Integer.parseInt(defaultVal);
    }

    /**
     * Clear folder with subfolders
     * */
    public static void clearFolder(Path unzippedPath) {
        try ( Stream<Path> pathStream = Files.walk(unzippedPath) ) {
            pathStream.sorted( Comparator.reverseOrder() )
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException ex) {
            logger.warn("unable clear the folder: {}", unzippedPath);
        }
        // create unzip dir
        try {
            Files.createDirectories(unzippedPath);
        } catch (IOException ex) {
            throw new FileCacheException("unable to create folder: " + unzippedPath, ex);
        }
    }

    /**
     * Un-ISO files from iso to destDir
     * */
    public static void isoToFolder(Path isoToRead, Path saveLocation) throws IOException {

        //Give the file and mention if this is treated as a read only file.
        try (Iso9660FileSystem discFs = new Iso9660FileSystem(isoToRead.toFile(), true)) {

            if (!Files.exists(saveLocation)) {
                logger.debug("not exists, creating: {}", saveLocation);
                Files.createDirectories(saveLocation);
            }

            //Go through each file on the disc and save it.
            for (Iso9660FileEntry singleFile : discFs) {
                if (singleFile.isDirectory()) {
                    Path dir = Paths.get(saveLocation.toString(), singleFile.getName());
                    Files.createDirectories(dir);
                } else {
                    Path tempFile = Paths.get(saveLocation.toString(), singleFile.getPath());
                    Files.copy(discFs.getInputStream(singleFile), tempFile, REPLACE_EXISTING);
                }
            }
        }
    }

    /**
     * Unzip files from zipPath to destDir
     */
    public static void unzipToFolder(Path zipPath, Path destDir) {

        try {
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath.toFile()));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir.toFile(), zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // write file content
                    writeBufferToFile(buffer, zis, newFile);
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (IOException ex) {
            throw new FileCacheException("unable to unzip: " + zipPath, ex);
        }
    }

    private static void writeBufferToFile(byte[] buffer, ZipInputStream zis, File newFile) throws IOException {

        try (FileOutputStream fos = new FileOutputStream(newFile)) {
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }
    }

    /**
     * Show ConfirmationDialog and wait for user' input
     *
     * Link https://betacode.net/11529/javafx-alert-dialog
     * */
    public static boolean getConfirmation(String question) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Внимание!");
        alert.setHeaderText(null);
        alert.setContentText(question);
        Optional<ButtonType> option = alert.showAndWait();
        return option.isPresent() && option.get() == ButtonType.OK;
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    /**
     * Показать пользователю диалог выбора наклейки для прожигаемого диска
     *
     * @return либо наклейку вида носителя (основной/запасной), либо пустую строку - пользователь не выбрал
     * */
    public static String getDiskLabel() {

        ButtonType btBase = new ButtonType("Основная копия");
        ButtonType btReserve = new ButtonType("Резервная копия");
        ButtonType btCancel =  new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Выбор типа носителя",
                btBase, btReserve, btCancel);
        alert.setTitle("Внимание!");
        alert.setHeaderText("Выберите метку, которая будет установлена на диск");

        ButtonType choice = alert.showAndWait().orElse(btCancel);
        if (btBase.equals(choice)) {
            return "Основной";
        } else if (btReserve.equals(choice)) {
            return "Резервный";
        }
        return "";
    }

    public static Properties loadProperties(String filePath) {

        Properties props = new Properties();
        try(InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
            props.load(inputStream);
        } catch (IOException ex) {
            logger.error( String.format("Unable to read settings from '%s'", filePath), ex );
            throw new FileCacheException(ex);
        }
        return props;
    }

    public static String readJsonFromFile(String fileName) {

        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        assert is != null;
        BufferedReader readr = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        return readr.lines().collect(Collectors.joining(System.getProperty("line.separator")));
    }
}
