package ru.vez.iso.desktop.document;

import javafx.collections.ObservableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.shared.AppStateData;
import ru.vez.iso.desktop.shared.AppStateType;
import ru.vez.iso.desktop.utils.UtilsHelper;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DocumentSrvImpl implements DocumentSrv {

    private static Logger logger = LogManager.getLogger();

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final Executor exec;
    private Future<Void> future = CompletableFuture.allOf();

    public DocumentSrvImpl(ObservableMap<AppStateType, AppStateData> appState, Executor exec) {
        this.appState = appState;
        this.exec = exec;
    }

    @Override
    public void loadAsync(Path path) {

        // Avoid multiply invocation
        if (!future.isDone()) {
            logger.debug("DocumentSrv.loadAsync: Async operation in progress, skipping");
            return;
        }
        future = CompletableFuture.supplyAsync(() -> {
            logger.debug("DocumentSrvImpl.loadAsync: Read from: " + path.toString());
            UtilsHelper.makeDelaySec(1);    // TODO load from file
            Random rnd = new Random();
            List<DocType> types = Collections.unmodifiableList(Arrays.asList(DocType.values()));
            List<BranchType> branches = Collections.unmodifiableList(Arrays.asList(BranchType.values()));
            List<DocStatus> statuses = Collections.unmodifiableList(Arrays.asList(DocStatus.values()));
            return IntStream.range(0, 20)
                    .mapToObj(i -> {
                        LocalDate date = LocalDate.of(1910+i, i%12+1, i%12+1);
                        DocumentFX doc = new DocumentFX(path + "-"+i, "docNumber-"+i, i, date,
                                types.get(rnd.nextInt(types.size())), date,
                                branches.get(rnd.nextInt(branches.size())),
                                statuses.get(rnd.nextInt(statuses.size())) );
                        doc.setSelected(i%2==0);
                        return doc;
                    })
                    .collect(Collectors.toList());
        }, exec).thenAccept(docs ->
                appState.put(AppStateType.DOCUMENTS, AppStateData.<List<DocumentFX>>builder().value(docs).build())
        ).exceptionally((ex) -> {
            logger.debug("Unable: " + ex.getLocalizedMessage());
            return null;
        } );
    }
}
