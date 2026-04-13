package com.arise.ss;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.List;
import java.util.Map;

public class MainApp extends Application {
    private TextArea logArea;
    private Label cleanLabel, suspiciousLabel, detectedLabel;
    private Button scanBtn;
    private SignatureManager sigManager = new SignatureManager();

    @Override
    public void start(Stage stage) {
        VBox root = new VBox(20);
        root.getStyleClass().add("ocean-bg");
        root.setPadding(new Insets(30));

        // Header
        Label title = new Label("ARISE SS TOOL");
        title.getStyleClass().add("header-title");

        // Agreement Overlay Logic
        showAgreement(stage);

        // Scan Controls
        scanBtn = new Button("START SCAN");
        ProgressBar pb = new ProgressBar(0);
        pb.setPrefWidth(500);

        // Results Panel
        HBox resultsBox = new HBox(20);
        cleanLabel = new Label("CLEAN: 0");
        suspiciousLabel = new Label("SUSPICIOUS: 0");
        detectedLabel = new Label("DETECTED: 0");
        resultsBox.getChildren().addAll(cleanLabel, suspiciousLabel, detectedLabel);

        // Log
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(200);

        scanBtn.setOnAction(e -> runScan(pb));

        root.getChildren().addAll(title, scanBtn, pb, resultsBox, new Label("Scan Log:"), logArea);
        
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setTitle("Arise SS Tool - Minecraft Verification");
        stage.show();
    }

    private void showAgreement(Stage owner) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Agreement Required");
        alert.setHeaderText("User Consent");
        alert.setContentText("This tool scans Minecraft directories and running processes. No personal files are accessed. Do you agree?");
        
        ButtonType agree = new ButtonType("I AGREE");
        ButtonType decline = new ButtonType("DECLINE", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(agree, decline);

        alert.showAndWait().ifPresent(type -> {
            if (type == decline) System.exit(0);
        });
    }

    private void runScan(ProgressBar pb) {
        ScannerEngine engine = new ScannerEngine(sigManager);
        pb.progressProperty().bind(engine.progressProperty());
        
        engine.messageProperty().addListener((obs, old, msg) -> logArea.appendText(msg + "\n"));
        
        engine.setOnSucceeded(e -> {
            Map<String, List<String>> res = engine.getValue();
            detectedLabel.setText("DETECTED: " + res.get("RED").size());
            suspiciousLabel.setText("SUSPICIOUS: " + res.get("YELLOW").size());
            if(res.get("RED").isEmpty() && res.get("YELLOW").isEmpty()) cleanLabel.setText("CLEAN: No Issues Found");
        });

        new Thread(engine).start();
    }

    public static void main(String[] args) { launch(args); }
}
