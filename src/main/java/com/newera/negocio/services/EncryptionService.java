package com.newera.negocio.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final String PREFIX = "ENC:";

    private final SecretKeySpec keySpec;

    public EncryptionService(@Value("${app.security.encryption-key:NovaCapital2026SecureMasterSecretKey!}") String secretKey) {
        try {
            // Generar clave AES de 256 bits consistente usando SHA-256 del secret
            byte[] keyBytes = MessageDigest.getInstance("SHA-256").digest(secretKey.getBytes(StandardCharsets.UTF_8));
            this.keySpec = new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando EncryptionService", e);
        }
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return plainText;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combinar IV + CipherText
            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return PREFIX + Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Error cifrando contenido", e);
        }
    }

    public String decrypt(String cipherTextWithPrefix) {
        if (cipherTextWithPrefix == null || !cipherTextWithPrefix.startsWith(PREFIX)) {
            // Si no tiene el prefijo ENC:, devolver tal cual (texto plano legacy o similar)
            return cipherTextWithPrefix;
        }
        try {
            String base64Data = cipherTextWithPrefix.substring(PREFIX.length());
            byte[] combined = Base64.getDecoder().decode(base64Data);

            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);

            int cipherTextSize = combined.length - IV_LENGTH;
            byte[] cipherText = new byte[cipherTextSize];
            System.arraycopy(combined, IV_LENGTH, cipherText, 0, cipherTextSize);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);

            byte[] plainTextBytes = cipher.doFinal(cipherText);
            return new String(plainTextBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Error descifrando contenido: " + e.getMessage());
            return cipherTextWithPrefix;
        }
    }

    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }
}
