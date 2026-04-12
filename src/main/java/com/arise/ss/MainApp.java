package com.arise.ss;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.awt.Desktop;
import java.net.URI;

public class MainApp extends Application {
    private TextArea logArea;
    private ProgressBar progressBar;
    private Label statusLabel;
    private StackPane contentArea;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-bg");

        // --- SIDEBAR ---
        VBox sidebar = new VBox(15);
        sidebar.setPrefWidth(200);
        sidebar.getStyleClass().add("sidebar");
        
        Label brand = new Label("ARISE ENGINE");
        brand.setStyle("-fx-text-fill: #00d4ff; -fx-font-weight: bold; -fx-font-size: 18px; -fx-padding: 0 0 20 0;");

        Button scanNav = createNavBtn("SCANNER");
        Button aboutNav = createNavBtn("ABOUT US");
        Button discordNav = createNavBtn("DISCORD");
        discordNav.setStyle("-fx-text-fill: #7289da;");

        sidebar.getChildren().addAll(brand, scanNav, aboutNav, discordNav);
        root.setLeft(sidebar);

        // --- MAIN CONTENT AREA ---
        contentArea = new StackPane();
        showScanner(); // Default view
        root.setCenter(contentArea);

        // Navigation Logic
        scanNav.setOnAction(e -> showScanner());
        aboutNav.setOnAction(e -> showAbout());
        discordNav.setOnAction(e -> openLink("https://discord.gg/yourlink"));

        Scene scene = new Scene(root, 1000, 650);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Arise SS Tool - Premium Edition");
        stage.show();
    }

    private void showScanner() {
        VBox scannerView = new VBox(20);
        scannerView.setPadding(new Insets(40));
        
        Label title = new Label("INTELLIGENT SYSTEM ANALYSIS");
        title.getStyleClass().add("header-text");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.getStyleClass().add("terminal-area");
        logArea.setPrefHeight(400);

        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        Button startBtn = new Button("INITIALIZE DEEP SCAN");
        startBtn.getStyleClass().add("scan-button");

        statusLabel = new Label("SYSTEM SECURED");
        statusLabel.setStyle("-fx-text-fill: #555;");

        scannerView.getChildren().addAll(title, logArea, progressBar, startBtn, statusLabel);
        
        startBtn.setOnAction(e -> {
            startBtn.setDisable(true);
            logArea.clear();
            new Thread(new ScannerEngine((progress, msg, severity) -> {
                Platform.runLater(() -> {
                    if (progress >= 0) progressBar.setProgress(progress);
                    if (msg != null) logArea.appendText(msg + "\n");
                    if (progress >= 1.0) {
                        startBtn.setDisable(false);
                        statusLabel.setText("REPORT GENERATED ON DESKTOP");
                    }
                });
            })).start();
        });

        contentArea.getChildren().setAll(scannerView);
    }

    private void showAbout() {
        VBox aboutView = new VBox(20);
        aboutView.setAlignment(Pos.CENTER);
        Label txt = new Label("Arise SS Tool v1.0.0\nDeveloped for professional Minecraft Server Staff.\nFocusing on transparency and deep detection.");
        txt.setStyle("-fx-text-fill: white; -fx-text-alignment: center; -fx-font-size: 16px;");
        aboutView.getChildren().add(txt);
        contentArea.getChildren().setAll(aboutView);
    }

    private Button createNavBtn(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("nav-button");
        return btn;
    }

    private void openLink(String url) {
        try { Desktop.getDesktop().browse(new URI(url)); } catch (Exception e) {}
    }

    public static void main(String[] args) { launch(args); }
}
