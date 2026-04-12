package com.arise.ss;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {
    private TextArea logArea;
    private ProgressBar progressBar;
    private Label statusLabel;

    @Override
    public void start(Stage stage) {
        // Use a BorderPane for a "Scanner Dashboard" look
        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-bg");

        // --- SIDEBAR ---
        VBox sidebar = new VBox(20);
        sidebar.setPrefWidth(180);
        sidebar.getStyleClass().add("sidebar");
        Label navTitle = new Label("ARISE ENGINE");
        navTitle.setStyle("-fx-text-fill: #555; -fx-font-weight: bold;");
        sidebar.getChildren().add(navTitle);
        root.setLeft(sidebar);

        // --- CENTER CONTENT ---
        VBox content = new VBox(25);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.CENTER_LEFT);

        Label header = new Label("SCANNER DASHBOARD");
        header.getStyleClass().add("header-text");

        logArea = new TextArea();
        logArea.setPrefHeight(300);
        logArea.getStyleClass().add("terminal-area");

        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        Button startBtn = new Button("INITIALIZE DEEP SCAN");
        startBtn.getStyleClass().add("scan-button");
        startBtn.setPrefHeight(45);
        startBtn.setPrefWidth(250);

        statusLabel = new Label("READY TO PROTECT");
        statusLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 14px;");

        content.getChildren().addAll(header, logArea, progressBar, startBtn, statusLabel);
        root.setCenter(content);

        startBtn.setOnAction(e -> {
            startBtn.setDisable(true);
            new Thread(new ScannerEngine((progress, msg, severity) -> {
                Platform.runLater(() -> {
                    if (progress >= 0) progressBar.setProgress(progress);
                    if (msg != null) logArea.appendText(msg + "\n");
                    if (progress >= 1.0) {
                        startBtn.setDisable(false);
                        statusLabel.setText("ANALYSIS FINISHED - CHECK DESKTOP");
                    }
                });
            })).start();
        });

        Scene scene = new Scene(root, 850, 550);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
