package com.arise.ss;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScannerEngine implements Runnable {
    public interface UpdateListener { void onUpdate(double progress, String msg, int severity); }
    private UpdateListener listener;
    private List<String> findings = new ArrayList<>();
    private int filesScanned = 0;
    private long ONE_DAY_MS = 24 * 60 * 60 * 1000;

    public ScannerEngine(UpdateListener listener) { this.listener = listener; }

    @Override
    public void run() {
        try {
            listener.onUpdate(0.1, "[*] Initializing Deep Heuristic Scan...", 0);
            
            // 1. Process Scan
            checkProcesses();
            
            // 2. Recursive File Scan
            File mcBase = new File(System.getProperty("user.home") + "/AppData/Roaming/.minecraft");
            deepScan(mcBase);
            
            // 3. Generate Advanced Report
            generateHtmlReport();

            int finalSeverity = findings.isEmpty() ? 0 : 2;
            listener.onUpdate(1.0, "[+] DEEP SCAN COMPLETE. FLAGS: " + findings.size(), finalSeverity);
        } catch (Exception e) {
            listener.onUpdate(0, "Error: " + e.getMessage(), 1);
        }
    }

    private void deepScan(File root) {
        if (root == null || !root.exists()) return;
        File[] list = root.listFiles();
        if (list == null) return;

        for (File f : list) {
            if (f.isDirectory()) {
                // Skip folders that are definitely safe to save time
                if (!f.getName().equals("assets") && !f.getName().equals("logs")) {
                    deepScan(f); 
                }
            } else {
                analyzeFile(f);
            }
        }
    }

    private void analyzeFile(File f) {
        filesScanned++;
        String name = f.getName().toLowerCase();
        long lastMod = f.lastModified();
        long now = System.currentTimeMillis();

        // 1. TIME HEURISTIC (Crucial for what you mentioned)
        if (now - lastMod < ONE_DAY_MS && (name.endsWith(".jar") || name.endsWith(".exe") || name.endsWith(".dll"))) {
            findings.add("CRITICAL: File modified in last 24h: " + f.getAbsolutePath());
        }

        // 2. STRING HEURISTIC (Looking inside the file)
        if (name.endsWith(".jar")) {
            scanJarInside(f);
        }
    }

    private void scanJarInside(File jarFile) {
        try (ZipFile zip = new ZipFile(jarFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                String entryName = entries.nextElement().getName().toLowerCase();
                // Common cheat package names/strings
                if (entryName.contains("clickgui") || entryName.contains("killaura") || 
                    entryName.contains("reach") || entryName.contains("exploit")) {
                    findings.add("HEURISTIC: Suspicious content inside " + jarFile.getName() + " -> " + entryName);
                    break; 
                }
            }
        } catch (Exception e) {
            // Not a valid zip or protected
        }
    }

    private void checkProcesses() {
        ProcessHandle.allProcesses().forEach(p -> {
            String cmd = p.info().command().orElse("").toLowerCase();
            if (cmd.contains("cheat") || cmd.contains("vape") || cmd.contains("injector")) {
                findings.add("PROCESS: Blacklisted application detected: " + cmd);
            }
        });
    }

    private void generateHtmlReport() {
        String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
        File reportFile = new File(desktopPath, "Arise_Result.html");
        
        String color = findings.isEmpty() ? "#00ff41" : "#ff3e3e";
        
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='background:#0a0a0c; color:white; font-family:sans-serif; padding:40px;'>");
        sb.append("<div style='border:1px solid #333; padding:20px; border-radius:10px; background:#111;'>");
        sb.append("<h1 style='color:#00d4ff;'>Arise Deep Scan Results</h1>");
        sb.append("<p>Status: <b style='color:"+color+"'>"+(findings.isEmpty()?"CLEAN":"THREATS DETECTED")+"</b></p>");
        sb.append("<p>Files Scanned: "+filesScanned+"</p>");
        sb.append("<hr style='border:0; border-top:1px solid #222;'>");
        
        for (String flag : findings) {
            sb.append("<div style='background:#1a1a1c; padding:10px; margin:10px 0; border-left:4px solid #ff3e3e;'>"+flag+"</div>");
        }
        
        if (findings.isEmpty()) sb.append("<p style='color:#888;'>No suspicious patterns found.</p>");
        
        sb.append("</div></body></html>");
        try (FileWriter fw = new FileWriter(reportFile)) { fw.write(sb.toString()); } catch (Exception e) {}
    }
}
