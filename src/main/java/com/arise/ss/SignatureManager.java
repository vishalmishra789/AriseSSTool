package com.arise.ss;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class SignatureManager {
    public List<CheatHash> blacklistedHashes = new ArrayList<>();
    public List<String> blacklistedProcesses = new ArrayList<>();
    public List<String> suspiciousStrings = new ArrayList<>();

    public static SignatureManager load() {
        // Look for signatures.json in the same folder where the EXE is running
        File jsonFile = new File("signatures.json");
        
        if (!jsonFile.exists()) {
            System.out.println("DEBUG: signatures.json not found in " + jsonFile.getAbsolutePath());
            return new SignatureManager(); // Return empty DB if file is missing
        }

        try (FileReader reader = new FileReader(jsonFile)) {
            return new Gson().fromJson(reader, SignatureManager.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new SignatureManager();
        }
    }

    public static class CheatHash {
        public String name;
        public String hash;
    }
}
