package textualmold9830.mppd.ui;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LogArea extends TextArea {
    public LogArea() {
        setEditable(false);
        setPrefHeight(150);
        setPromptText("Logs will appear here...");
    }

    public void log(String message) {
        Platform.runLater(() -> {
            appendText("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + message + "\n");
        });
    }
}
