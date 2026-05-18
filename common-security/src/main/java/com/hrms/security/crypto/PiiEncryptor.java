package com.hrms.security.crypto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-GCM symmetric encryption for sensitive employee fields (PAN, Aadhaar, bank account).
 * Key is derived from HRMS_ENCRYPTION_KEY (env). Output is base64 of [12-byte IV | ciphertext | 16-byte tag].
 * The encrypt() / decrypt() helpers are stateless and safe for use from JPA AttributeConverters.
 */
@Slf4j
@Component
public class PiiEncryptor {

    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;

    @Value("${hrms.security.encryption.key:hrms-default-32byte-encryption-key!!}")
    private String configuredKey;

    /** Returns a 256-bit key derived via SHA-256 from the configured value. */
    private SecretKey key() {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(configuredKey.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(hash, "AES");
        } catch (Exception e) { throw new IllegalStateException("Encryption init failed", e); }
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) return null;
        try {
            byte[] iv = new byte[IV_BYTES];
            new SecureRandom().nextBytes(iv);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] enc = c.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + enc.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(enc, 0, out, iv.length, enc.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            log.error("Encrypt failed: {}", e.getMessage());
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String cipherText) {
        if (cipherText == null) return null;
        try {
            byte[] all = Base64.getDecoder().decode(cipherText);
            if (all.length < IV_BYTES + 16) return cipherText;          // legacy unencrypted row
            byte[] iv = new byte[IV_BYTES];
            byte[] enc = new byte[all.length - IV_BYTES];
            System.arraycopy(all, 0, iv, 0, IV_BYTES);
            System.arraycopy(all, IV_BYTES, enc, 0, enc.length);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(c.doFinal(enc), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // If decryption fails the row is probably plaintext from before encryption was enabled — return as-is.
            return cipherText;
        }
    }
}
