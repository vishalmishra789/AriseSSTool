package com.arise.ss;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.*;

public class ScannerEngine implements Runnable {
    public interface UpdateListener { void onUpdate(double progress, String msg, int severity); }
    
    private UpdateListener listener;
    private SignatureManager db;
    private List<String> findings = new ArrayList<>();
    private int filesScanned = 0;
    private long ONEDAY = 24 * 60 * 60 * 1000;

    public ScannerEngine(UpdateListener listener) {
        this.listener = listener;
        this.db = SignatureManager.load();
    }

    @Override
    public void run() {
        try {
            listener.onUpdate(0.05, "[*] Booting Arise Deep-Search Engine...", 0);
            
            // 1. Scan Running Processes
            checkProcesses();
            
            // 2. Scan Minecraft Directory (Deep & Recursive)
            File mcFolder = new File(System.getProperty("user.home") + "/AppData/Roaming/.minecraft");
            recursiveScan(mcFolder);
            
            // 3. Finalize and Report
            generateHtmlReport();
            
            int sev = findings.isEmpty() ? 0 : 2;
            listener.onUpdate(1.0, "[+] Scan Finished. Findings saved to Desktop.", sev);
        } catch (Exception e) {
            listener.onUpdate(0, "Engine Error: " + e.getMessage(), 1);
        }
    }

    private void recursiveScan(File dir) {
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                // Ignore safe folders to save speed
                if (!f.getName().equalsIgnoreCase("assets") && !f.getName().equalsIgnoreCase("logs")) {
                    recursiveScan(f);
                }
            } else {
                analyzeFile(f);
            }
        }
    }

    private void analyzeFile(File f) {
        filesScanned++;
        String name = f.getName().toLowerCase();
        
        // Update UI every 50 files so it doesn't lag
        if (filesScanned % 50 == 0) {
            listener.onUpdate(-1, "[*] Analyzing: " + f.getName(), 0);
        }

        // HEURISTIC 1: Recency (Modified in last 24h)
        if (System.currentTimeMillis() - f.lastModified() < ONEDAY) {
            if (name.endsWith(".jar") || name.endsWith(".dll") || name.endsWith(".exe")) {
                findings.add("CRITICAL: Recently modified executable: " + f.getAbsolutePath());
            }
        }

        // HEURISTIC 2: Hash Database Check
        String fileHash = getFileHash(f);
        for (SignatureManager.CheatHash ch : db.blacklistedHashes) {
            if (ch.hash.equalsIgnoreCase(fileHash)) {
                findings.add("DATABASE MATCH: " + ch.name + " (" + f.getName() + ")");
            }
        }

        // HEURISTIC 3: Inside JAR String Search
        if (name.endsWith(".jar")) {
            scanJarContent(f);
        }
    }

    private void scanJarContent(File jar) {
        try (ZipFile zip = new ZipFile(jar)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                String entryName = entries.nextElement().getName().toLowerCase();
                for (String s : db.suspiciousStrings) {
                    if (entryName.contains(s)) {
                        findings.add("HEURISTIC: Found " + s + " inside " + jar.getName());
                        return; // Found one, move to next file
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void checkProcesses() {
        ProcessHandle.allProcesses().forEach(p -> {
            String cmd = p.info().command().orElse("").toLowerCase();
            for (String blacklisted : db.blacklistedProcesses) {
                if (cmd.contains(blacklisted)) {
                    findings.add("PROCESS MATCH: Active Blacklisted App -> " + blacklisted);
                }
            }
        });
    }

    private String getFileHash(File f) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] data = Files.readAllBytes(f.toPath());
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return ""; }
    }

    private void generateHtmlReport() {
        File report = new File(System.getProperty("user.home") + "/Desktop", "Arise_Report.html");
        String status = findings.isEmpty() ? "SECURE" : "DANGER";
        String color = findings.isEmpty() ? "#238636" : "#f85149";

        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><style>")
          .append("body{background:#0d1117; color:#c9d1d9; font-family:sans-serif; padding:50px;}")
          .append(".box{background:#161b22; border:1px solid #30363d; padding:30px; border-radius:10px;}")
          .append("h1{color:#58a6ff; margin:0;} .status{font-size:30px; color:"+color+"; font-weight:bold;}")
          .append(".flag{background:#010409; border-left:4px solid #f85149; padding:15px; margin:10px 0; color:#ffa657; font-family:monospace;}")
          .append("</style></head><body><div class='box'>")
          .append("<h1>ARISE INTELLIGENT REPORT</h1>")
          .append("<div class='status'>"+status+"</div>")
          .append("<p>Files Scanned: "+filesScanned+" | Flags Found: "+findings.size()+"</p><hr style='border:1px solid #30363d;'>");

        if(findings.isEmpty()) {
            sb.append("<p>No modifications or blacklisted signatures were detected.</p>");
        } else {
            for(String f : findings) sb.append("<div class='flag'>[!] " + f + "</div>");
        }

        sb.append("</div></body></html>");
        try (FileWriter fw = new FileWriter(report)) { fw.write(sb.toString()); } catch (Exception ignored) {}
    }
}
