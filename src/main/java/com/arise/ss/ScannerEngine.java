package com.arise.ss;

import javafx.concurrent.Task;
import java.io.File;
import java.util.*;

public class ScannerEngine extends Task<Map<String, List<String>>> {
    private final SignatureManager db;
    private final Map<String, List<String>> results = new HashMap<>();

    public ScannerEngine(SignatureManager db) {
        this.db = db;
        results.put("RED", new ArrayList<>());
        results.put("YELLOW", new ArrayList<>());
    }

    @Override
    protected Map<String, List<String>> call() throws Exception {
        updateMessage("Starting Scan...");
        
        // 1. Scan Processes
        updateMessage("Scanning active processes...");
        ProcessHandle.allProcesses().forEach(p -> {
            String name = p.info().command().orElse("").toLowerCase();
            checkMatch(name);
        });

        // 2. Scan Minecraft Directory
        String mcPath = System.getProperty("user.home") + "/AppData/Roaming/.minecraft";
        File mcDir = new File(mcPath);
        if (mcDir.exists()) {
            scanDirectory(new File(mcDir, "mods"));
            scanDirectory(new File(mcDir, "versions"));
        }

        updateProgress(1, 1);
        updateMessage("Scan Complete.");
        return results;
    }

    private void scanDirectory(File dir) {
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            updateMessage("Checking: " + f.getName());
            checkMatch(f.getName().toLowerCase());
            if (f.isDirectory()) scanDirectory(f);
        }
    }

    private void checkMatch(String name) {
        for (String s : db.getRedList()) {
            if (name.contains(s)) results.get("RED").add(name);
        }
        for (String s : db.getYellowList()) {
            if (name.contains(s)) results.get("YELLOW").add(name);
        }
    }
}
