package textualmold9830.mppd.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import textualmold9830.mppd.Manager;

public class BackupsTab extends Tab {
    private final ListView<String> backupsList;

    public BackupsTab(LogArea logArea, StatusArea statusArea) {
        super("Backups");
        setClosable(false);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        backupsList = new ListView<>();
        backupsList.setPrefHeight(300);
        
        HBox backupControls = new HBox(10);
        Button backupBtn = new Button("Create Backup");
        backupBtn.setOnAction(e -> runTask("Creating backup...", () -> {
            Manager.backup();
            Platform.runLater(() -> backupsList.getItems().setAll(Manager.getBackupsList()));
        }, logArea, statusArea));
        
        Button restoreBtn = new Button("Restore Selected");
        restoreBtn.setStyle("-fx-base: #b3d9ff;");
        restoreBtn.setOnAction(e -> {
            String selected = backupsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Restoring will OVERWRITE your current save. Continue?", ButtonType.YES, ButtonType.NO);
                if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                    runTask("Restoring " + selected + "...", () -> Manager.restoreBackup(selected), logArea, statusArea);
                }
            }
        });

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.setStyle("-fx-base: #ffcccc;");
        deleteBtn.setOnAction(e -> {
            String selected = backupsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (Manager.deleteBackup(selected)) {
                    logArea.log("Deleted backup: " + selected);
                    backupsList.getItems().setAll(Manager.getBackupsList());
                }
            }
        });

        backupControls.getChildren().addAll(backupBtn, restoreBtn, deleteBtn);
        
        Label info = new Label("Backups are stored in the 'backups' folder.");
        
        content.getChildren().addAll(new Label("Available Backups:"), backupsList, backupControls, info);
        setContent(content);
        setOnSelectionChanged(e -> {
            if (isSelected()) backupsList.getItems().setAll(Manager.getBackupsList());
        });
    }

    private void runTask(String status, Runnable task, LogArea logArea, StatusArea statusArea) {
        statusArea.setStatus(status);
        new Thread(() -> {
            try {
                task.run();
            } catch (Exception e) {
                logArea.log("Error: " + e.getMessage());
            }
            Platform.runLater(() -> {
                statusArea.setStatus("Ready");
                logArea.log("Task completed: " + status);
            });
        }).start();
    }
}
