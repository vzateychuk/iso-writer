package ru.vez.iso.desktop.document;

import javafx.collections.ObservableMap;
import ru.vez.iso.desktop.state.AppStateData;
import ru.vez.iso.desktop.state.AppStateType;
import ru.vez.iso.desktop.utils.UtilsHelper;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DocumentSrvImpl implements DocumentSrv {

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
            System.out.println("DocumentSrv.loadAsync: Async operation in progress, skipping");
            return;
        }
        System.out.println("DocumentSrvImpl.loadAsync: Read from: " + path.toString());
        future = CompletableFuture.supplyAsync(() -> {
            UtilsHelper.makeDelaySec(1);    // TODO load from file
            return IntStream.range(0, 10)
                    .mapToObj(i -> {
                        LocalDate date = LocalDate.of(1910+i, i+1, i+1);
                        DocumentFX doc = new DocumentFX("objectId-"+i, "docNumber-"+i, i, date,
                                DocType.ACCOUNT_CASH_WARRANT, date, BranchType.REGIONAL_BRANCH, DocStatus.MAIN_STATUS);
                        doc.setSelected(i%2==0);
                        return doc;
                    })
                    .collect(Collectors.toList());
        }, exec).thenAccept(docs ->
                appState.put(AppStateType.DOCUMENTS, AppStateData.<List<DocumentFX>>builder().value(docs).build())
        );
    }
}
