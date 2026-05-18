package com.hrms.asset.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.UUID;

/**
 * Generates a QR code (PNG) for any asset. The payload encodes a self-describing URL
 * the mobile scanner can POST to (e.g. for asset return / handover events).
 */
@Service
public class QrCodeService {

    public byte[] generatePng(String payload, int sizePx) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix m = writer.encode(payload, BarcodeFormat.QR_CODE, sizePx, sizePx);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(m, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("QR code generation failed: " + e.getMessage(), e);
        }
    }

    public String generateDataUri(String payload, int sizePx) {
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(generatePng(payload, sizePx));
    }

    /** Standard payload for an asset QR — opens the mobile UI to that asset. */
    public String assetUrl(String baseUrl, String tenantId, UUID assetId) {
        return baseUrl + "/m/asset/" + tenantId + "/" + assetId;
    }
}
