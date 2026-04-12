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
