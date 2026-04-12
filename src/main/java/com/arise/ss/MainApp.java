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
    private Button scanBtn;

    @Override
    public void start(Stage stage) {
        showConsentDialog(stage);
    }

    private void showConsentDialog(Stage stage) {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(30));
        box.getStyleClass().add("main-bg");

        Label title = new Label("ARISE SS TOOL - CONSENT");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: #00d4ff; -fx-font-weight: bold;");
        
        Label info = new Label("We scan: .minecraft/mods and active process names.\nWe NEVER scan: Documents, Photos, or Browser data.");
        info.setStyle("-fx-text-fill: white; -fx-text-alignment: center;");

        Button agree = new Button("I AGREE & START");
        agree.getStyleClass().add("primary-btn");
        agree.setOnAction(e -> initDashboard(stage));

        box.getChildren().addAll(title, info, agree);
        Scene scene = new Scene(box, 450, 300);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Arise SS - Consent");
        stage.show();
    }

    private void initDashboard(Stage stage) {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(25));
        layout.getStyleClass().add("main-bg");

        Label header = new Label("ARISE SS TOOL");
        header.setStyle("-fx-font-size: 24px; -fx-text-fill: #00d4ff; -fx-font-weight: bold;");

        scanBtn = new Button("INITIALIZE SYSTEM SCAN");
        scanBtn.setMaxWidth(Double.MAX_VALUE);
        scanBtn.getStyleClass().add("primary-btn");

        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(250);
        logArea.getStyleClass().add("log-area");

        statusLabel = new Label("STATUS: READY");
        statusLabel.setStyle("-fx-text-fill: #00ff41; -fx-font-weight: bold;");

        layout.getChildren().addAll(header, scanBtn, progressBar, logArea, statusLabel);

        scanBtn.setOnAction(e -> runScan());

        stage.setScene(new Scene(layout, 600, 500));
        stage.setTitle("Arise SS Tool - Professional Scanner");
    }

    private void runScan() {
        scanBtn.setDisable(true);
        ScannerEngine engine = new ScannerEngine((progress, msg, severity) -> {
            Platform.runLater(() -> {
                if (progress >= 0) progressBar.setProgress(progress);
                if (msg != null) logArea.appendText(msg + "\n");
                updateSeverity(severity);
                if (progress >= 1.0) scanBtn.setDisable(false);
            });
        });
        new Thread(engine).start();
    }

    private void updateSeverity(int severity) {
        if (severity == 1) { statusLabel.setText("STATUS: SUSPICIOUS"); statusLabel.setStyle("-fx-text-fill: #f1c40f;"); }
        else if (severity == 2) { statusLabel.setText("STATUS: DETECTED"); statusLabel.setStyle("-fx-text-fill: #e74c3c;"); }
    }

    public static void main(String[] args) { launch(args); }
}
