package com.arise.ss;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainApp extends Application {
    private TextArea logArea;
    private ProgressBar progressBar;
    private Label statusLabel;
    private Label filesScannedLabel;
    private Label threatsLabel;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("ocean-bg");

        // --- HEADER ---
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20));
        Label title = new Label("Arise Anti-Cheat");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #58a6ff;");
        Label sub = new Label("SYSTEM SCAN DASHBOARD");
        sub.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 11px;");
        
        HBox pills = new HBox(10);
        pills.setAlignment(Pos.CENTER);
        Label idLabel = new Label("INSTANCE: ACTIVE");
        idLabel.setStyle("-fx-background-color: #21262d; -fx-text-fill: #58a6ff; -fx-padding: 5 15; -fx-background-radius: 15; -fx-font-weight: bold;");
        statusLabel = new Label("STATUS: READY");
        statusLabel.setStyle("-fx-background-color: #238636; -fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 15; -fx-font-weight: bold;");
        pills.getChildren().addAll(idLabel, statusLabel);
        
        header.getChildren().addAll(title, sub, pills);
        root.setTop(header);

        // --- CENTER GRID (Ocean Style) ---
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.CENTER);

        // Cards
        grid.add(createCard("OPERATING SYSTEM", System.getProperty("os.name")), 0, 0);
        grid.add(createCard("USER ACCOUNT", System.getProperty("user.name")), 1, 0);
        
        filesScannedLabel = new Label("0");
        grid.add(createCard("FILES SCANNED", filesScannedLabel), 0, 1);
        
        threatsLabel = new Label("0");
        grid.add(createCard("FLAGS DETECTED", threatsLabel), 1, 1);

        root.setCenter(grid);

        // --- BOTTOM CONSOLE & SCAN ---
        VBox bottom = new VBox(15);
        bottom.setPadding(new Insets(20));
        
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(150);
        logArea.getStyleClass().add("ocean-console");

        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        Button scanBtn = new Button("LAUNCH OCEAN-GRADE SCAN");
        scanBtn.setMaxWidth(Double.MAX_VALUE);
        scanBtn.getStyleClass().add("ocean-btn");

        bottom.getChildren().addAll(logArea, progressBar, scanBtn);
        root.setBottom(bottom);

        // Scan Action
        scanBtn.setOnAction(e -> {
            scanBtn.setDisable(true);
            statusLabel.setText("SCANNING...");
            statusLabel.setStyle("-fx-background-color: #d29922; -fx-text-fill: black;");
            
            new Thread(new ScannerEngine((progress, msg, severity, count, threats) -> {
                Platform.runLater(() -> {
                    if (progress >= 0) progressBar.setProgress(progress);
                    if (msg != null) logArea.appendText(msg + "\n");
                    if (count >= 0) filesScannedLabel.setText(String.valueOf(count));
                    if (threats >= 0) threatsLabel.setText(String.valueOf(threats));
                    
                    if (progress >= 1.0) {
                        scanBtn.setDisable(false);
                        if (threats > 0) {
                            statusLabel.setText("CHEATING");
                            statusLabel.setStyle("-fx-background-color: #da3633; -fx-text-fill: white;");
                        } else {
                            statusLabel.setText("CLEAN");
                            statusLabel.setStyle("-fx-background-color: #238636; -fx-text-fill: white;");
                        }
                    }
                });
            })).start();
        });

        Scene scene = new Scene(root, 750, 650);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Ocean Style Anti-Cheat");
        stage.show();
    }

    private VBox createCard(String label, String value) {
        return createCard(label, new Label(value));
    }

    private VBox createCard(String label, Label valueLabel) {
        VBox card = new VBox(5);
        card.setPrefSize(340, 80);
        card.getStyleClass().add("ocean-card");
        
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 10px; -fx-font-weight: bold;");
        
        valueLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        card.getChildren().addAll(lbl, valueLabel);
        return card;
    }

    public static void main(String[] args) { launch(args); }
}
