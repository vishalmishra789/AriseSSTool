package com.arise.ss;

import com.google.gson.Gson;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class SignatureManager {
    public List<CheatHash> blacklistedHashes = new ArrayList<>();
    public List<String> blacklistedProcesses = new ArrayList<>();
    public List<String> suspiciousStrings = new ArrayList<>();

    public static SignatureManager load() {
        try (FileReader reader = new FileReader("signatures.json")) {
            return new Gson().fromJson(reader, SignatureManager.class);
        } catch (Exception e) {
            System.out.println("No signature database found, using heuristics only.");
            return new SignatureManager();
        }
    }

    public static class CheatHash {
        public String name;
        public String hash;
    }
}
