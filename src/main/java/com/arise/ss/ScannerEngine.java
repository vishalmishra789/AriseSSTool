package com.arise.ss;

import java.io.*;
import java.security.*;
import java.util.*;

public class ScannerEngine implements Runnable {
    public interface UpdateListener { void onUpdate(double progress, String msg, int severity); }
    
    private UpdateListener listener;
    private int maxSeverity = 0;

    public ScannerEngine(UpdateListener listener) { this.listener = listener; }

    @Override
    public void run() {
        try {
            listener.onUpdate(0.1, "[*] Scanning processes...", 0);
            checkProcesses();
            
            listener.onUpdate(0.4, "[*] Scanning .minecraft/mods...", 0);
            scanFiles();

            listener.onUpdate(1.0, "[+] Scan Finished. Report generated.", maxSeverity);
        } catch (Exception e) {
            listener.onUpdate(0, "[!] Scan Error: " + e.getMessage(), 1);
        }
    }

    private void checkProcesses() {
        ProcessHandle.allProcesses().forEach(p -> {
            String cmd = p.info().command().orElse("");
            if (cmd.contains("cheatengine") || cmd.contains("vape")) {
                maxSeverity = 2;
                listener.onUpdate(-1, "[!!!] Found Cheat Process: " + cmd, 2);
            }
        });
    }

    private void scanFiles() {
        String path = System.getProperty("user.home") + "/AppData/Roaming/.minecraft/mods";
        File folder = new File(path);
        if (!folder.exists()) return;

        for (File f : folder.listFiles()) {
            if (f.getName().endsWith(".jar")) {
                // Heuristic: Check for unusual file modification dates (recently modified)
                long lastMod = f.lastModified();
                if (System.currentTimeMillis() - lastMod < 3600000) { // Last hour
                    listener.onUpdate(-1, "[!] Recently modified mod: " + f.getName(), 1);
                    maxSeverity = Math.max(maxSeverity, 1);
                }
            }
        }
    }
}
