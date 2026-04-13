package com.arise.ss;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainApp extends Application {
    private TextArea logArea;
    private ListView<String> modsListView;
    private Label cleanLabel, suspiciousLabel, detectedLabel;
    private SignatureManager sigManager = new SignatureManager();

    @Override
    public void start(Stage stage) {
        TabPane tabPane = new TabPane();
        
        // --- TAB 1: SCANNER ---
        Tab scanTab = new Tab("Scanner");
        scanTab.setClosable(false);
        VBox scanLayout = new VBox(15);
        scanLayout.setPadding(new Insets(20));
        scanLayout.getStyleClass().add("ocean-bg");

        Button scanBtn = new Button("START DEEP SCAN");
        ProgressBar pb = new ProgressBar(0);
        pb.setPrefWidth(600);
        
        logArea = new TextArea();
        logArea.setPrefHeight(250);
        
        HBox stats = new HBox(20);
        detectedLabel = new Label("DETECTED: 0");
        suspiciousLabel = new Label("SUSPICIOUS: 0");
        cleanLabel = new Label("STATUS: Ready");
        stats.getChildren().addAll(detectedLabel, suspiciousLabel, cleanLabel);

        scanBtn.setOnAction(e -> startScan(pb));
        scanLayout.getChildren().addAll(new Label("Arise SS Tool - Detection Engine"), scanBtn, pb, stats, logArea);
        scanTab.setContent(scanLayout);

        // --- TAB 2: MODS IN USE ---
        Tab modsTab = new Tab("Mods Explorer");
        modsTab.setClosable(false);
        VBox modsLayout = new VBox(10);
        modsLayout.setPadding(new Insets(20));
        modsLayout.getStyleClass().add("ocean-bg");
        
        modsListView = new ListView<>();
        modsLayout.getChildren().addAll(new Label("Minecraft Mods Found in Directory:"), modsListView);
        modsTab.setContent(modsLayout);

        tabPane.getTabs().addAll(scanTab, modsTab);

        Scene scene = new Scene(tabPane, 850, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setTitle("Arise SS Tool");
        stage.show();
    }

    private void startScan(ProgressBar pb) {
        logArea.clear();
        modsListView.getItems().clear();
        
        ScannerEngine engine = new ScannerEngine(sigManager);
        pb.progressProperty().bind(engine.progressProperty());
        engine.messageProperty().addListener((obs, old, msg) -> logArea.appendText(msg + "\n"));

        engine.setOnSucceeded(e -> {
            ScanResult result = engine.getValue();
            detectedLabel.setText("DETECTED: " + result.redMatches.size());
            suspiciousLabel.setText("SUSPICIOUS: " + result.yellowMatches.size());
            modsListView.getItems().addAll(result.foundMods);
            
            if (result.redMatches.isEmpty()) {
                cleanLabel.setText("STATUS: CLEAN");
                cleanLabel.setStyle("-fx-text-fill: #64ffda;");
            } else {
                cleanLabel.setText("STATUS: THREATS FOUND");
                cleanLabel.setStyle("-fx-text-fill: #ff4d4d;");
            }
        });

        new Thread(engine).start();
    }

    public static void main(String[] args) { launch(args); }
}
