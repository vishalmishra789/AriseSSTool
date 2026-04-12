package com.arise.ss;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ScannerEngine implements Runnable {
    public interface UpdateListener { void onUpdate(double progress, String msg, int severity); }
    private UpdateListener listener;
    private List<String> findings = new ArrayList<>();

    public ScannerEngine(UpdateListener listener) { this.listener = listener; }

    @Override
    public void run() {
        try {
            listener.onUpdate(0.1, "[*] Initializing Arise SS Engine...", 0);
            
            // 1. Scan Processes
            listener.onUpdate(0.3, "[*] Analyzing system processes...", 0);
            checkProcesses();
            Thread.sleep(500);
            
            // 2. Scan Mods
            listener.onUpdate(0.6, "[*] Scanning .minecraft/mods directory...", 0);
            scanFiles();
            Thread.sleep(500);
            
            // 3. Save the Report
            saveReportToDesktop();

            listener.onUpdate(1.0, "[+] Scan Finished. Report saved to your Desktop.", 0);
        } catch (Exception e) {
            listener.onUpdate(0, "Error: " + e.getMessage(), 1);
        }
    }

    private void checkProcesses() {
        ProcessHandle.allProcesses().forEach(p -> {
            String cmd = p.info().command().orElse("");
            // Example detection
            if (cmd.toLowerCase().contains("cheatengine") || cmd.toLowerCase().contains("vape")) {
                findings.add("DETECTED PROCESS: " + cmd);
            }
        });
    }

    private void scanFiles() {
        String path = System.getProperty("user.home") + "/AppData/Roaming/.minecraft/mods";
        File modsFolder = new File(path);
        
        if (modsFolder.exists() && modsFolder.isDirectory()) {
            File[] files = modsFolder.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().toLowerCase().contains("vape") || f.getName().toLowerCase().contains("cheat")) {
                        findings.add("SUSPICIOUS FILE: " + f.getAbsolutePath());
                    }
                }
            }
        } else {
            findings.add("INFO: .minecraft/mods folder not found.");
        }
    }

    private void saveReportToDesktop() {
        String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
        File reportFile = new File(desktopPath, "AriseSS_Report.txt");
        
        try (FileWriter writer = new FileWriter(reportFile)) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            writer.write("==========================================\n");
            writer.write("        ARISE SS TOOL SCAN REPORT         \n");
            writer.write("==========================================\n");
            writer.write("Scan Time: " + dtf.format(LocalDateTime.now()) + "\n");
            writer.write("User: " + System.getProperty("user.name") + "\n\n");
            
            if (findings.isEmpty()) {
                writer.write("RESULT: CLEAN\n");
                writer.write("No suspicious modifications or processes detected.\n");
            } else {
                writer.write("RESULT: FLAGS DETECTED\n");
                for (String flag : findings) {
                    writer.write("- " + flag + "\n");
                }
            }
            writer.write("\n==========================================");
        } catch (IOException e) {
            listener.onUpdate(-1, "Failed to save report: " + e.getMessage(), 1);
        }
    }
}
