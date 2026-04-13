package com.arise.ss;

import javafx.concurrent.Task;
import java.io.File;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ScannerEngine extends Task<ScanResult> {
    private final SignatureManager db;
    private final ScanResult finalResult = new ScanResult();

    public ScannerEngine(SignatureManager db) {
        this.db = db;
    }

    @Override
    protected ScanResult call() throws Exception {
        updateMessage("Initializing deep scan...");
        
        // 1. Scan Processes
        ProcessHandle.allProcesses().forEach(p -> {
            String name = p.info().command().orElse("").toLowerCase();
            checkMatch(name, "Process: " + name);
        });

        // 2. Scan Minecraft Directory
        String mcPath = System.getProperty("user.home") + "/AppData/Roaming/.minecraft";
        File modsDir = new File(mcPath, "mods");
        
        if (modsDir.exists() && modsDir.isDirectory()) {
            File[] files = modsDir.listFiles();
            if (files != null) {
                int total = files.length;
                for (int i = 0; i < total; i++) {
                    File f = files[i];
                    if (f.getName().endsWith(".jar")) {
                        updateMessage("Analyzing Mod: " + f.getName());
                        finalResult.foundMods.add(f.getName()); // Add to the Mod Tab list
                        
                        // DEEP SCAN: Look inside the JAR
                        scanInsideJar(f);
                        
                        // Check filename too
                        checkMatch(f.getName().toLowerCase(), "File: " + f.getName());
                    }
                    updateProgress(i + 1, total);
                }
            }
        }

        updateMessage("Scan Complete.");
        return finalResult;
    }

    private void scanInsideJar(File jarFile) {
        try (ZipFile zip = new ZipFile(jarFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                String entryName = entries.nextElement().getName().toLowerCase();
                // Check if any cheat signatures exist as class names inside the jar
                for (String s : db.getRedList()) {
                    if (entryName.contains(s.replace(" ", ""))) {
                        finalResult.redMatches.add("Internal match in " + jarFile.getName() + " -> " + entryName);
                    }
                }
            }
        } catch (Exception e) {
            // Skip files that can't be opened
        }
    }

    private void checkMatch(String text, String context) {
        for (String s : db.getRedList()) {
            if (text.contains(s.toLowerCase())) finalResult.redMatches.add(context);
        }
        for (String s : db.getYellowList()) {
            if (text.contains(s.toLowerCase())) finalResult.yellowMatches.add(context);
        }
    }
}

// Helper class to hold results
class ScanResult {
    public List<String> redMatches = new ArrayList<>();
    public List<String> yellowMatches = new ArrayList<>();
    public List<String> foundMods = new ArrayList<>();
}
