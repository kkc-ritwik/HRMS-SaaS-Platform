package com.hrms.pdf.service;

import java.util.Map;

public interface PdfService {

    /** Render HTML to PDF bytes. */
    byte[] htmlToPdf(String html);

    /** Render an HTML template (mustache-style {{vars}}) to PDF bytes. */
    byte[] renderTemplate(String template, Map<String, Object> vars);

    /** Apply a centered diagonal text watermark. */
    byte[] watermark(byte[] pdf, String text);

    /** Add a digital signature placeholder field (signing happens externally). */
    byte[] signaturePlaceholder(byte[] pdf, String fieldName);

    /** Encrypt with owner + user passwords; userPassword may be null. */
    byte[] encrypt(byte[] pdf, String ownerPassword, String userPassword);

    /** Stamp a header / footer across all pages (page number is appended). */
    byte[] stampHeaderFooter(byte[] pdf, String header, String footer);
}
