package textualmold9830.mppd.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import textualmold9830.mppd.Manager;

public class PluginsTab extends Tab {
    private final ListView<String> pluginList;
    private final ListView<String> textureList;

    public PluginsTab(LogArea logArea) {
        super("Plugins & Textures");
        setClosable(false);
        
        pluginList = new ListView<>();
        textureList = new ListView<>();
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        HBox lists = new HBox(20);
        
        VBox pBox = new VBox(5);
        Label pLabel = new Label("Installed Plugins:");
        pLabel.setStyle("-fx-font-weight: bold;");
        pluginList.setPrefHeight(250);
        Button removePBtn = new Button("Remove Selected Plugin");
        removePBtn.setMaxWidth(Double.MAX_VALUE);
        removePBtn.setOnAction(e -> {
            String selected = pluginList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (Manager.removePlugin(selected)) {
                    logArea.log("Removed plugin: " + selected);
                    pluginList.getItems().setAll(Manager.getPluginsList());
                } else {
                    logArea.log("Failed to remove plugin: " + selected);
                }
            }
        });
        pBox.getChildren().addAll(pLabel, pluginList, removePBtn);
        HBox.setHgrow(pBox, Priority.ALWAYS);

        VBox tBox = new VBox(5);
        Label tLabel = new Label("Installed Textures:");
        tLabel.setStyle("-fx-font-weight: bold;");
        textureList.setPrefHeight(250);
        Button removeTBtn = new Button("Remove Selected Texture");
        removeTBtn.setMaxWidth(Double.MAX_VALUE);
        removeTBtn.setOnAction(e -> {
            String selected = textureList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (Manager.removeTexture(selected)) {
                    logArea.log("Removed texture: " + selected);
                    textureList.getItems().setAll(Manager.getTexturesList());
                } else {
                    logArea.log("Failed to remove texture: " + selected);
                }
            }
        });
        tBox.getChildren().addAll(tLabel, textureList, removeTBtn);
        HBox.setHgrow(tBox, Priority.ALWAYS);

        lists.getChildren().addAll(pBox, tBox);
        
        Button refreshBtn = new Button("Refresh Lists");
        refreshBtn.setOnAction(e -> refresh());

        content.getChildren().addAll(lists, refreshBtn);
        setContent(content);
        setOnSelectionChanged(e -> {
            if (isSelected()) refresh();
        });
    }

    private void refresh() {
        pluginList.getItems().setAll(Manager.getPluginsList());
        textureList.getItems().setAll(Manager.getTexturesList());
    }
}
