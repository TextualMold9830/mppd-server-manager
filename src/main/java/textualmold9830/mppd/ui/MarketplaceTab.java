package textualmold9830.mppd.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import textualmold9830.mppd.Manager;

public class MarketplaceTab extends Tab {
    private final ComboBox<String> typeBox;
    private final ListView<Manager.MarketItem> marketList;

    public MarketplaceTab(LogArea logArea, StatusArea statusArea) {
        super("Marketplace");
        setClosable(false);
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        typeBox = new ComboBox<>();
        typeBox.getItems().addAll("plugins", "textures");
        typeBox.setValue("plugins");

        marketList = new ListView<>();
        marketList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Manager.MarketItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox box = new HBox(10);
                    Label nameLabel = new Label(item.name != null ? item.name : item.id);
                    if (item.trusted) {
                        nameLabel.setText(nameLabel.getText() + " [✓]");
                        nameLabel.setTextFill(Color.GREEN);
                    } else if (item.name != null) {
                        nameLabel.setText(nameLabel.getText() + " [⚠️]");
                        nameLabel.setTextFill(Color.ORANGE);
                    }
                    box.getChildren().add(nameLabel);
                    setGraphic(box);
                }
            }
        });

        typeBox.valueProperty().addListener((obs, oldVal, newVal) -> loadMarketplace(logArea, statusArea));

        Button loadBtn = new Button("Refresh Marketplace");
        loadBtn.setOnAction(e -> loadMarketplace(logArea, statusArea));

        Button installBtn = new Button("Install Selected");
        installBtn.setOnAction(e -> {
            Manager.MarketItem item = marketList.getSelectionModel().getSelectedItem();
            if (item != null) {
                if (!item.trusted) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, 
                        "This plugin is from an untrusted developer. Proceed with caution.", 
                        ButtonType.YES, ButtonType.NO);
                    alert.setTitle("Security Warning");
                    if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.NO) return;
                }
                runTask("Installing " + item.name + "...", () -> {
                    Manager.installFromMarket(item, typeBox.getValue());
                }, logArea, statusArea);
            }
        });

        content.getChildren().addAll(new Label("Select Type:"), typeBox, loadBtn, marketList, installBtn);
        setContent(content);

        setOnSelectionChanged(e -> {
            if (isSelected() && marketList.getItems().isEmpty()) {
                loadMarketplace(logArea, statusArea);
            }
        });
        loadMarketplace(logArea, statusArea);
    }

    private void loadMarketplace(LogArea logArea, StatusArea statusArea) {
        runTask("Loading Marketplace...", () -> {
            java.util.List<Manager.MarketItem> items = Manager.getMarketItems(typeBox.getValue());
            java.util.List<Manager.MarketItem> detailedItems = new java.util.ArrayList<>();
            for (Manager.MarketItem item : items) {
                detailedItems.add(Manager.fetchItemDetails(item, typeBox.getValue()));
            }
            Platform.runLater(() -> marketList.getItems().setAll(detailedItems));
        }, logArea, statusArea);
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
