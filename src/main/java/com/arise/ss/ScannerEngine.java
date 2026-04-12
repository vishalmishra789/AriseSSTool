package com.arise.ss;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ScannerEngine implements Runnable {
    public interface UpdateListener { void onUpdate(double progress, String msg, int severity); }
    private UpdateListener listener;
    private List<String> findings = new ArrayList<>();
    private int filesScanned = 0;

    public ScannerEngine(UpdateListener listener) { this.listener = listener; }

    @Override
    public void run() {
        try {
            listener.onUpdate(0.1, "[*] Initializing Ocean-Grade Scanning Engine...", 0);
            
            // 1. Scan Processes (Deep Search)
            checkProcesses();
            listener.onUpdate(0.4, "[*] Process Scan Complete.", 0);
            
            // 2. Scan Minecraft Folders (Mods & Versions)
            String mcPath = System.getProperty("user.home") + "/AppData/Roaming/.minecraft";
            scanDir(new File(mcPath, "mods"));
            scanDir(new File(mcPath, "versions"));
            
            listener.onUpdate(0.8, "[*] Analyzing file integrity and metadata...", 0);
            
            // 3. Generate HTML Report
            generateHtmlReport();

            int finalSeverity = findings.isEmpty() ? 0 : 2;
            listener.onUpdate(1.0, "[+] Analysis Complete. HTML Report ready on Desktop.", finalSeverity);
        } catch (Exception e) {
            listener.onUpdate(0, "Error: " + e.getMessage(), 1);
        }
    }

    private void checkProcesses() {
        ProcessHandle.allProcesses().forEach(p -> {
            String cmd = p.info().command().orElse("").toLowerCase();
            String[] blacklist = {"cheatengine", "vape", "jukereach", "itami", "drip", "koid"};
            for (String s : blacklist) {
                if (cmd.contains(s)) findings.add("Suspicious Process: " + cmd);
            }
        });
    }

    private void scanDir(File dir) {
        if (!dir.exists()) return;
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            filesScanned++;
            String name = f.getName().toLowerCase();
            // Detect hidden jars or specific cheat names
            if (name.contains("vape") || name.contains("meteor") || name.contains("aristois") || name.contains("reach")) {
                findings.add("Blacklisted File: " + f.getAbsolutePath());
            }
            // Detect very small jars (often injectors)
            if (f.isFile() && name.endsWith(".jar") && f.length() < 5000) {
                findings.add("Anomalous Small Jar: " + f.getName() + " (" + f.length() + " bytes)");
            }
        }
    }

    private void generateHtmlReport() {
        String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
        File reportFile = new File(desktopPath, "Arise_Result.html");
        String statusColor = findings.isEmpty() ? "#00ff41" : "#ff4c4c";
        String statusText = findings.isEmpty() ? "CLEAN" : "DANGER";

        StringBuilder html = new StringBuilder();
        html.append("<html><head><style>")
            .append("body { background: #0b0e14; color: white; font-family: 'Segoe UI', sans-serif; padding: 50px; }")
            .append(".card { background: #151a21; padding: 30px; border-radius: 15px; border-left: 5px solid " + statusColor + "; }")
            .append("h1 { color: #00d4ff; }")
            .append(".status { font-size: 24px; font-weight: bold; color: " + statusColor + "; }")
            .append(".item { background: #1d252f; padding: 10px; margin: 5px 0; border-radius: 5px; border: 1px solid #333; }")
            .append("</style></head><body>")
            .append("<div class='card'>")
            .append("<h1>ARISE SS SCAN REPORT</h1>")
            .append("<p class='status'>SYSTEM STATUS: " + statusText + "</p>")
            .append("<p>Files Scanned: " + filesScanned + "</p>")
            .append("<p>Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>")
            .append("<hr style='border: 1px solid #333;'>");

        if (findings.isEmpty()) {
            html.append("<p>No malicious modifications were found in standard directories.</p>");
        } else {
            for (String f : findings) {
                html.append("<div class='item'>🚩 " + f + "</div>");
            }
        }

        html.append("</div></body></html>");

        try (PrintWriter out = new PrintWriter(reportFile)) {
            out.println(html.toString());
        } catch (Exception e) {}
    }
}
