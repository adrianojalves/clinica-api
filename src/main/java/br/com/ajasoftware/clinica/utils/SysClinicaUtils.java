package br.com.ajasoftware.clinica.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class SysClinicaUtils {
    /**
     * Generates a SHA-256 hash for a given string.
     * @param input Raw text (e.g., clean CPF).
     * @return 64-character hex string.
     */
    public static String generateSha256(String input) {
        if (input == null) return null;

        try {
            // Standardizes input by removing non-digits (for CPF/RG)
            String cleanInput = input.replaceAll("\\D", "");

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(cleanInput.getBytes(StandardCharsets.UTF_8));

            // HexFormat is a clean way available in modern Java (from Java 17+)
            return HexFormat.of().formatHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao processar hash de segurança.", e);
        }
    }
}
