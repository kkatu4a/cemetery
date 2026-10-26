package com.katu4a.cladbise;

import com.katu4a.cladbise.api.TestBackend;
import com.katu4a.cladbise.controller.*;
import com.katu4a.cladbise.util.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class JavaFxTest {
    private static TestBackend server;
    @BeforeAll static void start() throws Exception {
        server = new TestBackend();
        System.setProperty("cemetery.backendUrl", server.url());
        CountDownLatch started = new CountDownLatch(1);
        Platform.startup(() -> { Platform.setImplicitExit(false); started.countDown(); });
        assertTrue(started.await(10, TimeUnit.SECONDS));
    }
    @AfterAll static void stop() { server.close(); Platform.exit(); System.clearProperty("cemetery.backendUrl"); }
    private static <T> T fx(Callable<T> work) throws Exception {
        FutureTask<T> task = new FutureTask<>(work); Platform.runLater(task); return task.get(10, TimeUnit.SECONDS);
    }
    @Test void loadsMainViewAndAllForms() throws Exception {
        var roots = fx(() -> {
            var loaded = new java.util.ArrayList<Parent>();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
            var tabs = (javafx.scene.control.TabPane) ((javafx.scene.layout.BorderPane) root).getCenter();
            tabs.getTabs().forEach(tab -> loaded.add((Parent) tab.getContent()));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            root.applyCss(); root.layout();
            for (String name : new String[]{"customer-form", "employee-form", "plot-form", "request-dialog"}) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + name + ".fxml"));
                Parent form = loader.load(); Scene formScene = new Scene(form);
                formScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                loaded.add(form);
                Object controller = loader.getController();
                if (controller instanceof CustomerFormController c) c.init(BackendProvider.getCustomerService(), null);
                if (controller instanceof EmployeeFormController c) c.init(BackendProvider.getEmployeeService(), null);
                if (controller instanceof PlotFormController c) c.init(BackendProvider.getPlotService(), null);
                if (controller instanceof RequestDialogController c) c.setRequestRow(null);
                form.applyCss(); form.layout();
            }
            return loaded;
        });
        long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < until && fx(() -> roots.stream().anyMatch(
                root -> root.getProperties().containsKey(FxAsync.class.getName())))) Thread.sleep(10);
        assertTrue(fx(() -> roots.stream().noneMatch(root -> root.getProperties().containsKey(FxAsync.class.getName()))));
        assertNotNull(server.path);
    }

    @Test void buttonStylesApplyWithoutFontConversionWarnings() throws Exception {
        var warnings = new java.util.concurrent.ConcurrentLinkedQueue<String>();
        var logger = java.util.logging.Logger.getLogger("");
        var handler = new java.util.logging.Handler() {
            public void publish(java.util.logging.LogRecord record) {
                if (record.getLevel().intValue() >= java.util.logging.Level.WARNING.intValue())
                    warnings.add(record.getMessage());
            }
            public void flush() {}
            public void close() {}
        };
        logger.addHandler(handler);
        try {
            fx(() -> {
                VBox root = new VBox();
                for (String style : new String[]{"button", "primary-button", "button-primary", "danger-button", "button-danger"}) {
                    var button = new javafx.scene.control.Button(style);
                    button.getStyleClass().add(style); root.getChildren().add(button);
                }
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                root.applyCss(); root.layout();
                for (var child : root.getChildren()) {
                    var button = (javafx.scene.control.Button) child;
                    assertEquals(13, button.getFont().getSize());
                    button.setDisable(true); root.applyCss();
                    button.setDisable(false); root.applyCss();
                }
                return null;
            });
            assertTrue(warnings.stream().noneMatch(message -> message.contains("-fx-font") || message.contains("ClassCastException")),
                    () -> String.join("\n", warnings));
        } finally { logger.removeHandler(handler); }
    }

    @Test void workerRunsOffFxThreadAndRejectsDuplicateSubmission() throws Exception {
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        CompletableFuture<Boolean> result = new CompletableFuture<>(); AtomicInteger operations = new AtomicInteger();
        VBox root = fx(() -> {
            Label anchor = new Label(); VBox pane = new VBox(anchor);
            FxAsync.run(anchor, () -> {
                operations.incrementAndGet(); entered.countDown();
                assertFalse(Platform.isFxApplicationThread());
                assertTrue(release.await(5, TimeUnit.SECONDS)); return 42;
            }, value -> result.complete(Platform.isFxApplicationThread() && value == 42 && !pane.isDisable()), result::completeExceptionally);
            FxAsync.run(anchor, () -> { operations.incrementAndGet(); return 0; }, ignored -> {}, result::completeExceptionally);
            assertTrue(pane.isDisable()); return pane;
        });
        assertTrue(entered.await(5, TimeUnit.SECONDS)); release.countDown();
        assertTrue(result.get(5, TimeUnit.SECONDS)); assertEquals(1, operations.get());
        assertFalse(fx(root::isDisable));
    }

    @Test void failureReenablesFormOnFxThread() throws Exception {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        fx(() -> {
            Label label = new Label(); VBox pane = new VBox(label);
            FxAsync.run(label, () -> { throw new IllegalStateException("network failed"); },
                    ignored -> result.complete(false), failure -> result.complete(
                            Platform.isFxApplicationThread() && !pane.isDisable() && failure.getMessage().equals("network failed")));
            return null;
        });
        assertTrue(result.get(5, TimeUnit.SECONDS));
    }
}
