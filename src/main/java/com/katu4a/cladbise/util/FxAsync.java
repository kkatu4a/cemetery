package com.katu4a.cladbise.util;

import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/** Invoked on the FX thread. Only the callable runs on a worker thread. */
public final class FxAsync {
    private static final String BUSY = FxAsync.class.getName();
    private FxAsync() {}
    public static <T> void run(Node anchor, Callable<T> operation, Consumer<T> success, Consumer<Throwable> failure) {
        Node root = anchor;
        while (root.getParent() != null) root = root.getParent();
        if (Boolean.TRUE.equals(root.getProperties().get(BUSY))) return;
        Node target = root;
        boolean disabled = target.isDisable();
        Cursor previous = target.getCursor();
        target.getProperties().put(BUSY, true);
        target.setDisable(true);
        target.setCursor(Cursor.WAIT);
        HBox progress = new HBox(8, new ProgressIndicator(), new Label("Выполняется запрос…"));
        progress.setStyle("-fx-background-color: white; -fx-padding: 8; -fx-border-color: #bbbbbb;");
        progress.setManaged(false);
        progress.setMouseTransparent(true);
        ((ProgressIndicator) progress.getChildren().getFirst()).setPrefSize(20, 20);
        if (target instanceof Pane pane) {
            pane.getChildren().add(progress);
            progress.relocate(8, 8);
            progress.autosize();
        }
        Task<T> task = new Task<>() {
            @Override protected T call() throws Exception { return operation.call(); }
        };
        Runnable release = () -> {
            target.getProperties().remove(BUSY);
            target.setDisable(disabled);
            target.setCursor(previous);
            if (target instanceof Pane pane) pane.getChildren().remove(progress);
        };
        task.setOnSucceeded(event -> { release.run(); success.accept(task.getValue()); });
        task.setOnFailed(event -> { release.run(); failure.accept(task.getException()); });
        task.setOnCancelled(event -> release.run());
        Thread worker = new Thread(task, "backend-request");
        worker.setDaemon(true);
        worker.start();
    }
}
