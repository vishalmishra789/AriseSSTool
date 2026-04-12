package com.arise.ss;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainApp extends Application {
    private TextArea logArea;
    private ProgressBar progressBar;
    private Label statusLabel;
    private Button scanBtn;
    private VBox mainLayout;

    @Override
    public void start(Stage stage) {
        showConsentScreen(stage);
    }

    private void showConsentScreen(Stage stage) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("main-bg");

        Label title = new Label("ARISE SS TOOL");
        title.getStyleClass().add("header-text");
        
        Label info = new Label("By clicking AGREE, you authorize a secure scan of Minecraft files.\nNo personal data will be collected.");
        info.setWrapText(true);
        info.getStyleClass().add("info-text");

        Button agreeBtn = new Button("AGREE & CONTINUE");
        agreeBtn.getStyleClass().add("action-button");
        
        // Button Hover Animation
        addHoverAnimation(agreeBtn);

        agreeBtn.setOnAction(e -> {
            fadeOut(root, () -> showDashboard(stage));
        });

        root.getChildren().addAll(title, info, agreeBtn);
        Scene scene = new Scene(root, 500, 400);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        
        stage.setScene(scene);
        stage.setTitle("Arise SS Tool");
        fadeIn(root);
        stage.show();
    }

    private void showDashboard(Stage stage) {
        mainLayout = new VBox(25);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setPadding(new Insets(30));
        mainLayout.getStyleClass().add("main-bg");

        Label header = new Label("SYSTEM SCANNER");
        header.getStyleClass().add("header-text");

        scanBtn = new Button("INITIALIZE DEEP SCAN");
        scanBtn.setMaxWidth(Double.MAX_VALUE);
        scanBtn.getStyleClass().add("scan-button");
        addHoverAnimation(scanBtn);

        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(20);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.getStyleClass().add("terminal-area");

        statusLabel = new Label("SYSTEM STATUS: SECURE");
        statusLabel.getStyleClass().add("status-ready");

        mainLayout.getChildren().addAll(header, scanBtn, progressBar, logArea, statusLabel);
        
        Scene scene = new Scene(mainLayout, 700, 550);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        
        stage.setScene(scene);
        fadeIn(mainLayout);

        scanBtn.setOnAction(e -> startAdvancedScan());
    }

    private void startAdvancedScan() {
        scanBtn.setDisable(true);
        statusLabel.setText("STATUS: SCANNING...");
        statusLabel.setStyle("-fx-text-fill: #ffcc00;");

        ScannerEngine engine = new ScannerEngine((progress, msg, severity) -> {
            Platform.runLater(() -> {
                if (progress >= 0) progressBar.setProgress(progress);
                if (msg != null) logArea.appendText(msg + "\n");
                
                if (progress >= 1.0) {
                    scanBtn.setDisable(false);
                    statusLabel.setText("STATUS: SCAN COMPLETE");
                    applyGlowEffect(statusLabel, Color.LIME);
                }
            });
        });
        new Thread(engine).start();
    }

    // --- ANIMATION UTILS ---

    private void fadeIn(VBox node) {
        FadeTransition ft = new FadeTransition(Duration.millis(800), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void fadeOut(VBox node, Runnable onFinished) {
        FadeTransition ft = new FadeTransition(Duration.millis(500), node);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(e -> onFinished.run());
        ft.play();
    }

    private void addHoverAnimation(Button btn) {
        btn.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), btn);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        btn.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), btn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    private void applyGlowEffect(Label label, Color color) {
        DropShadow glow = new DropShadow();
        glow.setColor(color);
        glow.setRadius(20);
        glow.setSpread(0.5);
        label.setEffect(glow);
    }

    public static void main(String[] args) { launch(args); }
}
