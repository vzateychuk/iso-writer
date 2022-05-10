package ru.vez.iso.desktop.shared;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UtilsHelper {

    private static final Logger logger = LogManager.getLogger();

    private UtilsHelper() {}

    public static void makeDelaySec(int delay) {
        try {
            Thread.sleep(delay * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
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

        int val = 1;
        try{
            val = Integer.parseInt(text);
            if (val < 1 || val > 100) {
                logger.warn("Incorrect value {}, default {} will be used", text, defaultVal);
                val = Integer.parseInt(defaultVal);
            }
        } catch (NumberFormatException ex){
            logger.warn("unable to convert to int, value: " + text);
            val = Integer.parseInt(defaultVal);
        }
        return val;
    }

    /**
     * Clear folder with subfolders
     * */
    public static void clearFolder(Path unzippedPath) {
        try {
            Files.walk(unzippedPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException ex) {
            logger.warn("unable clear the folder: {}", unzippedPath);
        }
        // create unzip dir
        try {
            Files.createDirectories(unzippedPath);
        } catch (IOException ex) {
            throw new RuntimeException("unable to create folder: " + unzippedPath, ex);
        }
    }

    public static void unzipToFolder(Path destDir, Path zipPath) {

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
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (IOException ex) {
            throw new RuntimeException("unable to unzip: " + zipPath, ex);
        }
    }

    /**
     * @See https://betacode.net/11529/javafx-alert-dialog
     * */
    public static boolean getConfirmation(String question) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Внимание!");
        alert.setHeaderText(null);
        alert.setContentText(question);
        Optional<ButtonType> option = alert.showAndWait();
        return option.isPresent() && option.get() == ButtonType.OK;
    }

    //region PRIVATE

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    //endregion

}
