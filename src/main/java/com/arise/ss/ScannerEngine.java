To match the Ocean Anti-Cheat interface and report depth shown in your screenshots, we need to completely rebuild both the application and the generated report.

We will implement a Grid Card Layout for the GUI and a Minecraft Log Analyzer that reads the latest.log file to find traces of self-destructed cheats or forced class loads.

Here is the full setup to get this high-fidelity, Ocean-style system.

1. The Ocean-Style GUI (MainApp.java)

This layout mimics the exact grid structure, pill-style status indicators, and sleek dark aesthetic of Ocean.

code
Java
download
content_copy
expand_less
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
2. The Scanner & Minecraft Log Analyzer (ScannerEngine.java)

This script contains the actual Log Analyzer and generates the exact visual report shown in your PDF.

code
Java
download
content_copy
expand_less
package com.arise.ss;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

public class ScannerEngine implements Runnable {
    public interface UpdateListener { void onUpdate(double progress, String msg, int severity, int count, int threats); }
    private UpdateListener listener;
    
    private List<String> findings = new ArrayList<>();
    private List<String> logFindings = new ArrayList<>();
    private int filesScanned = 0;

    public ScannerEngine(UpdateListener listener) { this.listener = listener; }

    @Override
    public void run() {
        try {
            listener.onUpdate(0.1, "[*] Analyzing PC Information...", 0, 0, 0);
            Thread.sleep(1000);

            // 1. Deep File Scan
            listener.onUpdate(0.3, "[*] Scanning Game Directory...", 0, 0, 0);
            File mcBase = new File(System.getProperty("user.home") + "/AppData/Roaming/.minecraft");
            recursiveScan(mcBase);

            // 2. Log Analyzer (NEW)
            listener.onUpdate(0.7, "[*] Analyzing Minecraft Logs...", 0, filesScanned, findings.size());
            analyzeGameLogs(new File(mcBase, "logs/latest.log"));

            // 3. Generate Ocean-Style HTML Report
            generateOceanReport();

            listener.onUpdate(1.0, "[+] Scan Finished. Check Desktop.", 0, filesScanned, findings.size());
        } catch (Exception e) {
            listener.onUpdate(0, "Error: " + e.getMessage(), 1, 0, 0);
        }
    }

    private void recursiveScan(File dir) {
        if (dir == null || !dir.exists()) return;
        File[] list = dir.listFiles();
        if (list == null) return;

        for (File f : list) {
            filesScanned++;
            if (f.isDirectory()) {
                if (!f.getName().equals("assets")) recursiveScan(f);
            } else {
                String name = f.getName().toLowerCase();
                // Flag recently modified files (within 1 day)
                if (System.currentTimeMillis() - f.lastModified() < 86400000 && (name.endsWith(".jar") || name.endsWith(".dll"))) {
                    findings.add("Evidence Cleaning / Modification detected in: " + f.getName());
                }
            }
            if (filesScanned % 100 == 0) {
                listener.onUpdate(-1, null, 0, filesScanned, findings.size());
            }
        }
    }

    private void analyzeGameLogs(File logFile) {
        if (!logFile.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Common cheat traces in logs
                if (line.contains("Using forced class load") || line.contains("Vape") || line.contains("Reach")) {
                    logFindings.add("Log Trace Detected: " + line);
                }
            }
        } catch (IOException ignored) {}
    }

    private void generateOceanReport() throws Exception {
        File report = new File(System.getProperty("user.home") + "/Desktop", "Ocean_Style_Report.html");
        String status = findings.isEmpty() && logFindings.isEmpty() ? "CLEAN" : "CHEATING";
        String color = status.equals("CLEAN") ? "#238636" : "#da3633";

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><style>")
          .append("body { background: #010409; color: #c9d1d9; font-family: 'Segoe UI', sans-serif; padding: 40px; }")
          .append(".header { text-align: center; margin-bottom: 30px; }")
          .append(".pill { display: inline-block; padding: 5px 20px; border-radius: 15px; font-weight: bold; background: "+color+"; color: white; }")
          .append(".card { background: #0d1117; border: 1px solid #30363d; border-radius: 6px; padding: 20px; margin-bottom: 20px; }")
          .append(".card-title { color: #58a6ff; font-weight: bold; margin-bottom: 15px; border-bottom: 1px solid #21262d; padding-bottom: 5px; }")
          .append(".threat-box { background: #161b22; border-left: 4px solid #da3633; padding: 15px; margin-bottom: 10px; border-radius: 4px; }")
          .append(".log-box { background: #0d1117; font-family: monospace; padding: 10px; color: #8b949e; border: 1px solid #30363d; }")
          .append(".grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }")
          .append("</style></head><body>")
          
          .append("<div class='header'>")
          .append("<h1 style='color:white; margin:0;'>Arise Anti-Cheat</h1>")
          .append("<p style='color:#8b949e;'>System Scan Analysis Report</p>")
          .append("<div class='pill'>"+status+"</div>")
          .append("</div>")

          .append("<div class='card'><div class='card-title'>PC Information</div>")
          .append("<div class='grid'>")
          .append("<div><b>OS:</b> "+System.getProperty("os.name")+"</div>")
          .append("<div><b>User:</b> "+System.getProperty("user.name")+"</div>")
          .append("<div><b>Scan Time:</b> "+LocalDateTime.now()+"</div>")
          .append("<div><b>Total Files Scanned:</b> "+filesScanned+"</div>")
          .append("</div></div>");

        if (!findings.isEmpty()) {
            sb.append("<div class='card'><div class='card-title'>Detection Results ("+findings.size()+")</div>");
            for (String f : findings) {
                sb.append("<div class='threat-box'><b>Suspicious File Activity Detected:</b><br>"+f+"</div>");
            }
            sb.append("</div>");
        }

        if (!logFindings.isEmpty()) {
            sb.append("<div class='card'><div class='card-title'>Minecraft Log Analyzer Results ("+logFindings.size()+")</div>");
            for (String l : logFindings) {
                sb.append("<div class='threat-box' style='border-left-color: #d29922;'><b>Suspicious Log Event:</b><br>"+l+"</div>");
            }
            sb.append("</div>");
        }

        sb.append("</body></html>");

        try (FileWriter fw = new FileWriter(report)) { fw.write(sb.toString()); }
    }
}
3. The Ocean Theme CSS (style.css)

Put this into src/main/resources/style.css to handle the grid layout and card structures.

code
CSS
download
content_copy
expand_less
.ocean-bg {
    -fx-background-color: #010409;
}

.ocean-card {
    -fx-background-color: #0d1117;
    -fx-border-color: #30363d;
    -fx-border-width: 1px;
    -fx-border-radius: 6px;
    -fx-background-radius: 6px;
    -fx-padding: 15;
}

.ocean-console {
    -fx-control-inner-background: #0d1117;
    -fx-text-fill: #58a6ff;
    -fx-font-family: "Consolas", monospace;
    -fx-font-size: 11px;
    -fx-border-color: #30363d;
    -fx-border-radius: 4px;
}

.ocean-btn {
    -fx-background-color: #21262d;
    -fx-text-fill: #c9d1d9;
    -fx-font-weight: bold;
    -fx-border-color: #30363d;
    -fx-border-radius: 6px;
    -fx-background-radius: 6px;
    -fx-padding: 12 30;
    -fx-cursor: hand;
}

.ocean-btn:hover {
    -fx-background-color: #30363d;
}

.progress-bar .track {
    -fx-background-color: #21262d;
    -fx-background-radius: 10;
}

.progress-bar .bar {
    -fx-background-color: #238636;
    -fx-background-radius: 10;
}
What this gives you:

The Grid Layout: The GUI now uses a two-column card system just like Ocean's "PC Information" section.

Pill Badges: The GUI features colored pills ("CLEAN", "READY", "CHEATING") instead of plain text labels.

Log Analyzer: It specifically scans the latest.log file for forced class loading exceptions.

HTML Output: The generated report now copies Ocean's style with a massive outer box and color-coded threats on the left side.
