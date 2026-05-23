package com.hrms.storage.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * Defence-in-depth file upload validation:
 *   1. Size cap (per-tenant overridable).
 *   2. MIME-type whitelist (declared content-type).
 *   3. Magic-byte sniffing — blocks an attacker who relabels a .exe as .pdf.
 *   4. Filename sanitisation — strips path traversal, control chars.
 *
 * Antivirus scan is delegated to {@link AntivirusScanner}; call both for every upload.
 */
@Slf4j
@Component
public class FileSecurityValidator {

    @Value("${hrms.storage.upload.max-bytes:52428800}")
    private long maxBytes;            // 50 MB default

    @Value("${hrms.storage.upload.allowed-mime:application/pdf,image/jpeg,image/png,image/gif,image/webp,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,text/csv,text/plain,application/zip,application/json}")
    private String allowedMimeCsv;

    /** Common magic signatures we recognise. */
    private static final Map<String, byte[][]> MAGIC = Map.ofEntries(
            Map.entry("application/pdf",          new byte[][]{{0x25, 0x50, 0x44, 0x46}}),                                   // %PDF
            Map.entry("image/jpeg",               new byte[][]{{(byte)0xFF, (byte)0xD8, (byte)0xFF}}),
            Map.entry("image/png",                new byte[][]{{(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}}),
            Map.entry("image/gif",                new byte[][]{{0x47, 0x49, 0x46, 0x38}}),                                    // GIF8
            Map.entry("image/webp",               new byte[][]{{0x52, 0x49, 0x46, 0x46}}),                                    // RIFF
            Map.entry("application/zip",          new byte[][]{{0x50, 0x4B, 0x03, 0x04}, {0x50, 0x4B, 0x05, 0x06}}),
            Map.entry("application/msword",       new byte[][]{{(byte)0xD0, (byte)0xCF, 0x11, (byte)0xE0}}),                  // OLE2
            Map.entry("application/vnd.ms-excel", new byte[][]{{(byte)0xD0, (byte)0xCF, 0x11, (byte)0xE0}})
    );

    public void validate(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new ValidationException("File is empty");
        if (file.getSize() > maxBytes) {
            throw new ValidationException("File exceeds max size " + maxBytes + " bytes");
        }
        String declaredMime = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        Set<String> allowed = new HashSet<>(Arrays.asList(allowedMimeCsv.toLowerCase(Locale.ROOT).split(",")));
        if (!allowed.contains(declaredMime)) {
            throw new ValidationException("MIME type not allowed: " + declaredMime);
        }
        // ZIP-based Office docs (docx/xlsx) share PK magic
        byte[][] expectedSigs = MAGIC.get(declaredMime);
        if (declaredMime.endsWith("wordprocessingml.document")
                || declaredMime.endsWith("spreadsheetml.sheet")) {
            expectedSigs = MAGIC.get("application/zip");
        }
        if (expectedSigs != null) {
            byte[] head = readHead(file, 16);
            boolean ok = false;
            for (byte[] sig : expectedSigs) {
                if (startsWith(head, sig)) { ok = true; break; }
            }
            if (!ok) throw new ValidationException(
                    "Magic-byte mismatch: declared " + declaredMime + " but content does not match");
        }
        log.debug("File {} ({} bytes, {}) passed validation",
                file.getOriginalFilename(), file.getSize(), declaredMime);
    }

    public String sanitizeFilename(String name) {
        if (name == null) return "file";
        String cleaned = name.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_");
        if (cleaned.contains("..")) cleaned = cleaned.replace("..", "_");
        if (cleaned.length() > 200) cleaned = cleaned.substring(cleaned.length() - 200);
        return cleaned;
    }

    private byte[] readHead(MultipartFile file, int n) throws IOException {
        byte[] buf = new byte[Math.min(n, (int) file.getSize())];
        try (var in = file.getInputStream()) {
            int read = 0;
            while (read < buf.length) {
                int r = in.read(buf, read, buf.length - read);
                if (r < 0) break;
                read += r;
            }
        }
        return buf;
    }

    private static boolean startsWith(byte[] data, byte[] sig) {
        if (data.length < sig.length) return false;
        for (int i = 0; i < sig.length; i++) if (data[i] != sig[i]) return false;
        return true;
    }

    public static class ValidationException extends RuntimeException {
        public ValidationException(String msg) { super(msg); }
    }
}
