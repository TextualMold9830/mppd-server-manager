package textualmold9830.mppd.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import textualmold9830.mppd.Manager;

public class ConfigTab extends Tab {
    private final TextArea configArea;

    public ConfigTab(LogArea logArea, StatusArea statusArea) {
        super("Configuration");
        setClosable(false);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label label = new Label("Server Settings (config.json)");
        label.setStyle("-fx-font-weight: bold;");
        
        configArea = new TextArea();
        configArea.setPrefHeight(350);
        
        Button saveBtn = new Button("Save Config");
        saveBtn.setPrefWidth(120);
        saveBtn.setOnAction(e -> {
            if (Manager.saveConfigRaw(configArea.getText())) {
                logArea.log("Config saved successfully.");
                statusArea.setStatus("Config Updated");
            } else {
                logArea.log("Failed to save config. Check JSON format.");
            }
        });

        content.getChildren().addAll(label, configArea, saveBtn);
        setContent(content);
        setOnSelectionChanged(e -> {
            if (isSelected()) configArea.setText(Manager.getConfigRaw());
        });
    }

    public TextArea getConfigArea() {
        return configArea;
    }
}
