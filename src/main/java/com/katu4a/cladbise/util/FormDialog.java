package com.katu4a.cladbise.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.function.Consumer;

public final class FormDialog {

    private FormDialog() {
    }

    public static <C extends SavableForm> boolean show(Window owner, String fxmlPath, String title,
                                                       Consumer<C> initializer) {
        try {
            FXMLLoader loader = new FXMLLoader(FormDialog.class.getResource(fxmlPath));
            Parent root = loader.load();
            C controller = loader.getController();
            initializer.accept(controller);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(FormDialog.class.getResource("/css/styles.css").toExternalForm());

            Stage dialog = new Stage();
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(title);
            dialog.setResizable(false);
            dialog.setScene(scene);
            dialog.showAndWait();

            return controller.isSaved();
        } catch (IOException e) {
            AlertHelper.showError(owner, "Не удалось открыть форму: " + e.getMessage());
            return false;
        }
    }
}