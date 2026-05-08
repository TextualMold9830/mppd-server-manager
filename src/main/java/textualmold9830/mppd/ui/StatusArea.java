package textualmold9830.mppd.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class StatusArea extends VBox {
    private final Label statusLabel;

    public StatusArea() {
        statusLabel = new Label("Status: Ready");
        statusLabel.setStyle("-fx-font-weight: bold;");
        getChildren().add(statusLabel);
    }

    public void setStatus(String status) {
        statusLabel.setText("Status: " + status);
    }
}
