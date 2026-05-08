package textualmold9830.mppd.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import textualmold9830.mppd.Manager;

public class DashboardTab extends Tab {
    public DashboardTab(LogArea logArea, StatusArea statusArea) {
        super("Dashboard");
        setClosable(false);

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Label title = new Label("MPPD Server Control");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER);

        Button installBtn = new Button("Install/Update");
        installBtn.setPrefWidth(120);
        installBtn.setOnAction(e -> runTask("Installing/Updating...", Manager::install, logArea, statusArea));

        Button startBtn = new Button("Start Server");
        startBtn.setPrefWidth(120);
        startBtn.setStyle("-fx-base: #b3ffb3;");
        startBtn.setOnAction(e -> {
            logArea.log("Starting server...");
            Manager.start(new String[0]);
            statusArea.setStatus("Server Running");
        });

        Button stopBtn = new Button("Stop Server");
        stopBtn.setPrefWidth(120);
        stopBtn.setStyle("-fx-base: #ffb3b3;");
        stopBtn.setOnAction(e -> {
            logArea.log("Stopping server...");
            Manager.stop();
            statusArea.setStatus("Server Stopped");
        });

        controls.getChildren().addAll(installBtn, startBtn, stopBtn);
        content.getChildren().addAll(title, new Separator(), controls);
        setContent(content);
        setOnSelectionChanged(e -> { if(isSelected()) logArea.log("Dashboard selected."); });
    }

    private void runTask(String status, Runnable task, LogArea logArea, StatusArea statusArea) {
        statusArea.setStatus(status);
        new Thread(() -> {
            try {
                task.run();
            } catch (Exception e) {
                logArea.log("Error: " + e.getMessage());
            }
            javafx.application.Platform.runLater(() -> {
                statusArea.setStatus("Ready");
                logArea.log("Task completed: " + status);
            });
        }).start();
    }
}
