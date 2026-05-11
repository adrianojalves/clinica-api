package br.com.ajasoftware.clinica.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for AES-256-GCM encryption and decryption.
 * High-security standard for sensitive data (LGPD compliance).
 */
@Component
public class CryptoUtils {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final int AES_KEY_BIT = 256;

    @Value("${api.security.crypto.key}")
    private String secretKey;

    /**
     * Encrypts a string value using AES-256-GCM.
     * @param strToEncrypt Raw string.
     * @return Base64 encoded string containing IV + CipherText.
     */
    public String encrypt(String strToEncrypt) {
        try {
            byte[] iv = new byte[IV_LENGTH_BYTE];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);
            byte[] cipherText = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));

            // Combine IV and CipherText into a single byte array for storage
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar dado sensível.", e);
        }
    }

    /**
     * Decrypts a Base64 string back to raw text.
     * @param strToDecrypt Base64 string (IV + CipherText).
     * @return Decrypted raw string.
     */
    public String decrypt(String strToDecrypt) {
        try {
            byte[] decode = Base64.getDecoder().decode(strToDecrypt);

            ByteBuffer byteBuffer = ByteBuffer.wrap(decode);
            byte[] iv = new byte[IV_LENGTH_BYTE];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");

            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar dado sensível.", e);
        }
    }
}