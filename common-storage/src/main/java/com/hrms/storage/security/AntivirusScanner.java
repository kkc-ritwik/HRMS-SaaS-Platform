package com.hrms.storage.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * ClamAV INSTREAM scanner client. Mode is controlled by {@code hrms.storage.antivirus.mode}:
 *   · OFF    — default; scan is a no-op (for dev/test).
 *   · STUB   — log the scan, reject anything containing the EICAR test signature.
 *   · CLAMAV — speak the clamd INSTREAM protocol over TCP. Requires a clamd container
 *              reachable at {@code hrms.storage.antivirus.host:port} (defaults clamav:3310).
 *
 * Production deploy: run clamav/clamav as a sidecar or DaemonSet and set mode=CLAMAV.
 */
@Slf4j
@Component
public class AntivirusScanner {

    @Value("${hrms.storage.antivirus.mode:OFF}")
    private String mode;
    @Value("${hrms.storage.antivirus.host:clamav}")
    private String host;
    @Value("${hrms.storage.antivirus.port:3310}")
    private int port;
    @Value("${hrms.storage.antivirus.timeout-ms:30000}")
    private int timeoutMs;

    private static final String EICAR = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*";

    public ScanResult scan(MultipartFile file) {
        if ("OFF".equalsIgnoreCase(mode)) return ScanResult.clean("DISABLED");
        try (InputStream in = file.getInputStream()) {
            if ("STUB".equalsIgnoreCase(mode)) return stubScan(in);
            if ("CLAMAV".equalsIgnoreCase(mode)) return clamavInstream(in);
            return ScanResult.clean("UNKNOWN-MODE");
        } catch (Exception e) {
            log.error("Antivirus scan failed", e);
            return ScanResult.infected("SCAN_ERROR:" + e.getMessage());
        }
    }

    private ScanResult stubScan(InputStream in) throws IOException {
        byte[] head = in.readNBytes(2048);
        String s = new String(head, StandardCharsets.UTF_8);
        if (s.contains(EICAR)) return ScanResult.infected("EICAR-Test-Signature");
        return ScanResult.clean("STUB-OK");
    }

    /**
     * clamd INSTREAM protocol:
     *   client → "zINSTREAM\0"
     *   for each chunk: 4-byte BE length + chunk
     *   final: 4-byte zero length
     *   server → "stream: OK\0" or "stream: <virus> FOUND\0"
     */
    private ScanResult clamavInstream(InputStream in) throws IOException {
        try (Socket sock = new Socket()) {
            sock.connect(new java.net.InetSocketAddress(host, port), timeoutMs);
            sock.setSoTimeout(timeoutMs);
            OutputStream out = sock.getOutputStream();
            out.write("zINSTREAM\0".getBytes(StandardCharsets.US_ASCII));
            byte[] buf = new byte[8192];
            int read;
            while ((read = in.read(buf)) > 0) {
                ByteBuffer len = ByteBuffer.allocate(4).putInt(read);
                out.write(len.array());
                out.write(buf, 0, read);
            }
            out.write(new byte[]{0, 0, 0, 0});
            out.flush();

            byte[] resp = sock.getInputStream().readNBytes(4096);
            String response = new String(resp, StandardCharsets.US_ASCII).trim();
            if (response.endsWith("OK")) return ScanResult.clean(response);
            return ScanResult.infected(response);
        }
    }

    public record ScanResult(boolean clean, String detail) {
        public static ScanResult clean(String detail)    { return new ScanResult(true, detail); }
        public static ScanResult infected(String detail) { return new ScanResult(false, detail); }
        public boolean isInfected() { return !clean; }
    }
}
