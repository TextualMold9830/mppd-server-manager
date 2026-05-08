package textualmold9830.mppd;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import textualmold9830.mppd.ui.*;

public class MainUI extends Application {

    private LogArea logArea;
    private StatusArea statusArea;
    private ConfigTab configTab;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("MPPD Server Manager");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        
        statusArea = new StatusArea();
        logArea = new LogArea();
        configTab = new ConfigTab(logArea, statusArea);

        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(new DashboardTab(logArea, statusArea));
        tabPane.getTabs().add(configTab);
        tabPane.getTabs().add(new PluginsTab(logArea));
        tabPane.getTabs().add(new MarketplaceTab(logArea, statusArea));
        tabPane.getTabs().add(new BackupsTab(logArea, statusArea));

        root.getChildren().addAll(tabPane, new Separator(), statusArea, logArea);

        Scene scene = new Scene(root, 700, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        logArea.log("Manager UI started.");
        autoLoadData();
    }

    private void autoLoadData() {
        logArea.log("Auto-loading server data...");
        configTab.getConfigArea().setText(Manager.getConfigRaw());
        logArea.log("Data loaded from installation.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
