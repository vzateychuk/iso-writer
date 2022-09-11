package ru.vez.iso.desktop.main.filecache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.exceptions.FileCacheException;
import ru.vez.iso.desktop.shared.AppSettings;
import ru.vez.iso.desktop.shared.FileISO;
import ru.vez.iso.desktop.state.ApplicationState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public List<FileISO> readFileCache(String dir) {

        logger.debug("dir: {}", dir);

        Path path = Paths.get(dir);
        List<Path> fileNames = this.readAndFilter(
                path,
                (p, a) -> p.toString().endsWith(".iso") && a.isRegularFile()
                && !state.isLoading(p.getFileName().toString())    // and file is not loading now
        );

        return fileNames.stream()
                .map(this::newFileInfo)
                .collect(Collectors.toList());
    }

    private FileISO newFileInfo(Path path) {

        String name = path.getFileName().toString();
        LocalDateTime createdAt = LocalDateTime.now();
        try {
            BasicFileAttributes fileAttr = Files.readAttributes(path, BasicFileAttributes.class);
            createdAt =  LocalDateTime.ofInstant( fileAttr.creationTime().toInstant(), ZoneId.systemDefault());
        } catch (IOException ex) {
            logger.error("unable read file info: {}", path, ex);
        }
        return new FileISO(name, createdAt.toLocalDate());
    }

    @Override
    public String deleteFile(String fileName) {

        AppSettings sets = state.getSettings();
        Path filePath = Paths.get(sets.getIsoCachePath(), fileName);
        try {
            Files.delete(filePath);
        } catch (IOException ex) {
            throw new FileCacheException("unable to delete file: " + filePath, ex);
        }
        return sets.getIsoCachePath();
    }

    //region PRIVATE

    List<Path> readAndFilter(Path path, BiPredicate<Path, BasicFileAttributes> filter) {

        try ( Stream<Path> pathStream = Files.find(path, 1, filter) )
        {
            return pathStream.collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Unable to read: {}", path);
            throw new FileCacheException(e);
        }
    }

    //endregion
}
