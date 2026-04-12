package com.arise.ss;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

public class ScannerEngine implements Runnable {
    public interface UpdateListener { void onUpdate(double progress, String msg, int severity); }
    private UpdateListener listener;
    private List<String> findings = new ArrayList<>();
    private SignatureManager db = SignatureManager.load();

    public ScannerEngine(UpdateListener listener) { this.listener = listener; }

    @Override
    public void run() {
        try {
            listener.onUpdate(0.05, "[*] Launching Ocean-Grade Heuristics...", 0);
            
            // 1. Check for JVM Agent injection
            checkJvmArgs();

            // 2. Full Minecraft Deep Scan (Everything: libs, versions, mods)
            File mcBase = new File(System.getProperty("user.home") + "/AppData/Roaming/.minecraft");
            scanRecursively(mcBase);
            
            // 3. Generate HTML
            new ReportGenerator().generate(findings);
            
            listener.onUpdate(1.0, "[+] Scan Finished. Found " + findings.size() + " threats.", 0);
        } catch (Exception e) {
            listener.onUpdate(0, "Error: " + e.getMessage(), 1);
        }
    }

    private void checkJvmArgs() {
        // This detects clients injected via -javaagent or custom flags
        String classPath = System.getProperty("java.class.path");
        if (classPath.toLowerCase().contains("cheat") || classPath.toLowerCase().contains("dd")) {
            findings.add("INJECTION: Malicious string found in ClassPath: " + classPath);
        }
    }

    private void scanRecursively(File dir) {
        if (dir == null || !dir.exists() || findings.size() > 50) return;
        File[] list = dir.listFiles();
        if (list == null) return;

        for (File f : list) {
            if (f.isDirectory()) {
                // We scan everything now, including libraries!
                scanRecursively(f);
            } else {
                analyzeFile(f);
            }
        }
    }

    private void analyzeFile(File f) {
        String name = f.getName().toLowerCase();
        
        // RECENCY CHECK: Flag any exe/jar/dll modified in the last 12 hours
        if (System.currentTimeMillis() - f.lastModified() < (12 * 60 * 60 * 1000)) {
            if (name.endsWith(".jar") || name.endsWith(".dll")) {
                findings.add("CRITICAL RECENCY: Modified today -> " + f.getAbsolutePath());
            }
        }

        // DEEP SCAN: If it's a JAR, look for "DD", "Vape", "Reach" classes inside
        if (name.endsWith(".jar")) {
            try (ZipFile zip = new ZipFile(f)) {
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    String entry = entries.nextElement().getName().toLowerCase();
                    if (entry.contains("killaura") || entry.contains("ddclient") || entry.contains("reach")) {
                        findings.add("HEURISTIC: Found cheat code in " + f.getName() + " (" + entry + ")");
                    }
                }
            } catch (Exception e) {}
        }
    }
}
