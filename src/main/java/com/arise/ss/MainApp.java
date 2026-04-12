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
    private Label fileCountLabel;
    private Label statusLabel;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-bg");

        // --- SIDEBAR ---
        VBox sidebar = new VBox(25);
        sidebar.setPrefWidth(200);
        sidebar.getStyleClass().add("sidebar");
        
        Label sideTitle = new Label("SCAN STATS");
        sideTitle.getStyleClass().add("side-label");
        
        fileCountLabel = new Label("0");
        fileCountLabel.getStyleClass().add("stat-value");
        Label fileText = new Label("FILES ANALYZED");
        fileText.setStyle("-fx-text-fill: #484f58; -fx-font-size: 10px;");

        statusLabel = new Label("IDLE");
        statusLabel.getStyleClass().add("stat-value");
        statusLabel.setStyle("-fx-text-fill: #238636;");

        sidebar.getChildren().addAll(sideTitle, fileCountLabel, fileText, new Separator(), statusLabel);
        root.setLeft(sidebar);

        // --- MAIN CONTENT ---
        VBox center = new VBox(20);
        center.setPadding(new Insets(30));
        
        Label header = new Label("ARISE INTELLIGENT SCANNER");
        header.getStyleClass().add("header-text");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.getStyleClass().add("terminal-area");
        logArea.setPrefHeight(350);

        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        Button scanBtn = new Button("LAUNCH DEEP ANALYSIS");
        scanBtn.getStyleClass().add("scan-button");

        center.getChildren().addAll(header, logArea, progressBar, scanBtn);
        root.setCenter(center);

        scanBtn.setOnAction(e -> {
            scanBtn.setDisable(true);
            logArea.clear();
            statusLabel.setText("RUNNING");
            statusLabel.setStyle("-fx-text-fill: #d29922;");

            new Thread(new ScannerEngine((progress, msg, severity) -> {
                Platform.runLater(() -> {
                    if (progress >= 0) progressBar.setProgress(progress);
                    if (msg != null) logArea.appendText(msg + "\n");
                    if (progress >= 1.0) {
                        scanBtn.setDisable(false);
                        statusLabel.setText("FINISHED");
                        statusLabel.setStyle("-fx-text-fill: #238636;");
                    }
                });
            })).start();
        });

        stage.setScene(new Scene(root, 950, 600));
        stage.setTitle("Arise SS Tool - v1.0.0");
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
