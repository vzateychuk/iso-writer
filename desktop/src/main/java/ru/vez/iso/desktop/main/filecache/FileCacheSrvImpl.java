package ru.vez.iso.desktop.main.filecache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.shared.AppSettings;
import ru.vez.iso.desktop.shared.IsoFileFX;
import ru.vez.iso.desktop.state.ApplicationState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * Работа с файловым cache
 * */
public class FileCacheSrvImpl implements FileCacheSrv {

    private static final Logger logger = LogManager.getLogger();

    private final ApplicationState state;

    public FileCacheSrvImpl( ApplicationState state ) {
        this.state = state;
    }

    @Override
    public List<IsoFileFX> readFileCache(String dir) {

        logger.debug("dir: {}", dir);

        Path path = Paths.get(dir);
        List<String> fileNames = this.readAndFilter(path, 1, (p,a) -> p.toString().endsWith(".iso") && a.isRegularFile() )
                .stream().map(p -> p.getFileName().toString()).collect(Collectors.toList());

        return fileNames.stream()
                .sorted(String::compareTo)
                .map(IsoFileFX::new)
                .collect(Collectors.toList());

    }

    @Override
    public String deleteFile(String fileName) {

        AppSettings sets = state.getSettings();
        Path filePath = Paths.get(sets.getIsoCachePath(), fileName);
        try {
            Files.delete(filePath);
        } catch (IOException ex) {
            logger.warn("unable to delete file: {}", filePath, ex);
            throw new RuntimeException("unable to delete file: " + filePath);
        }
        return sets.getIsoCachePath();
    }

    //region PRIVATE

    List<Path> readAndFilter(Path path, int depth, BiPredicate<Path, BasicFileAttributes> filter) {

        try {
            return Files.find(path, depth, filter).collect(Collectors.toList());
        } catch (IOException e) {
            logger.warn("Unable to read: " + path);
            throw new RuntimeException(e);
        }
    }

    //endregion
}
