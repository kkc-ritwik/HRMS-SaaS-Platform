package com.hrms.expense.ocr;

import com.hrms.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Receipt OCR via Apache Tika. Best-effort extraction of:
 *   - merchant name (first non-empty header line)
 *   - total amount (largest currency-looking number)
 *   - date (first ISO / DD-MM-YYYY pattern)
 *   - currency (₹ / $ / EUR / etc.)
 *   - GSTIN (if any)
 *   - line items (lines with "<text> ... <amount>")
 *
 * Tika handles PDF receipts natively. For image receipts (jpg/png), Tika delegates
 * to tesseract if installed locally — falls back gracefully if not.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptOcrService {

    private static final Pattern TOTAL_LINE = Pattern.compile(
            "(?i)\\b(total|grand\\s*total|amount\\s*due|amount\\s*payable|net\\s*amount|invoice\\s*total)\\b[^0-9]*([0-9,]+\\.?[0-9]*)");
    private static final Pattern ANY_AMOUNT = Pattern.compile("[₹$€£]\\s?([0-9,]+\\.?[0-9]{0,2})|([0-9,]{2,}\\.[0-9]{2})");
    private static final Pattern DATE_DDMMYYYY = Pattern.compile("(\\d{1,2}[-/.](?:\\d{1,2}|[A-Za-z]{3,9})[-/.]\\d{2,4})");
    private static final Pattern DATE_ISO = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
    private static final Pattern CURRENCY_SYM = Pattern.compile("(₹|INR|Rs\\.?|\\$|USD|€|EUR|£|GBP|AED|SGD|JPY)", Pattern.CASE_INSENSITIVE);
    private static final Pattern GSTIN = Pattern.compile("[0-3][0-9][A-Z]{5}[0-9]{4}[A-Z][0-9A-Z]{3}");

    private final StorageService storage;
    private final Tika tika = new Tika();
    private final AutoDetectParser parser = new AutoDetectParser();

    public OcrResult parseUpload(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            return parseStream(in, file.getOriginalFilename(), file.getContentType());
        } catch (Exception e) {
            log.warn("Receipt OCR failed for {}: {}", file.getOriginalFilename(), e.getMessage());
            return OcrResult.empty();
        }
    }

    public OcrResult parseStoredFile(String storageUri) {
        try (InputStream in = storage.download(storageUri)) {
            return parseStream(in, storageUri, null);
        } catch (Exception e) {
            log.warn("Receipt OCR failed for {}: {}", storageUri, e.getMessage());
            return OcrResult.empty();
        }
    }

    private OcrResult parseStream(InputStream in, String filename, String contentType) throws Exception {
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata meta = new Metadata();
        parser.parse(in, handler, meta);
        String text = handler.toString();
        return extract(text, filename);
    }

    public String detectMimeType(InputStream in, String filename) {
        try { return tika.detect(in, filename == null ? "" : filename); }
        catch (Exception e) { return "application/octet-stream"; }
    }

    private OcrResult extract(String text, String filename) {
        String merchant = guessMerchant(text);
        BigDecimal total = extractTotal(text);
        LocalDate date = extractDate(text);
        String currency = extractCurrency(text);
        String gstin = matchFirst(GSTIN, text);
        List<LineItem> items = extractLineItems(text);
        return new OcrResult(merchant, total, date, currency, gstin, items,
                text.length() > 20000 ? text.substring(0, 20000) : text);
    }

    private String guessMerchant(String text) {
        for (String line : text.split("\\R")) {
            String t = line.trim();
            if (t.length() > 4 && t.length() < 60
                    && t.chars().anyMatch(Character::isLetter)
                    && !t.toLowerCase().contains("invoice")
                    && !t.toLowerCase().contains("receipt"))
                return t;
        }
        return null;
    }

    private BigDecimal extractTotal(String text) {
        Matcher m = TOTAL_LINE.matcher(text);
        BigDecimal best = null;
        while (m.find()) {
            try {
                BigDecimal v = new BigDecimal(m.group(2).replace(",", ""));
                if (best == null || v.compareTo(best) > 0) best = v;
            } catch (NumberFormatException ignored) {}
        }
        if (best != null) return best;
        // fallback: largest currency-looking number
        Matcher m2 = ANY_AMOUNT.matcher(text);
        while (m2.find()) {
            String raw = m2.group(1) != null ? m2.group(1) : m2.group(2);
            try {
                BigDecimal v = new BigDecimal(raw.replace(",", ""));
                if (best == null || v.compareTo(best) > 0) best = v;
            } catch (NumberFormatException ignored) {}
        }
        return best;
    }

    private LocalDate extractDate(String text) {
        Matcher m = DATE_ISO.matcher(text);
        if (m.find()) try { return LocalDate.parse(m.group(1)); } catch (Exception ignored) {}
        m = DATE_DDMMYYYY.matcher(text);
        if (m.find()) {
            String s = m.group(1);
            for (DateTimeFormatter f : List.of(
                    DateTimeFormatter.ofPattern("d-M-yyyy"), DateTimeFormatter.ofPattern("d/M/yyyy"),
                    DateTimeFormatter.ofPattern("d-MMM-yyyy"), DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                    DateTimeFormatter.ofPattern("d.M.yyyy"))) {
                try { return LocalDate.parse(s, f); } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private String extractCurrency(String text) {
        String c = matchFirst(CURRENCY_SYM, text);
        if (c == null) return null;
        return switch (c.toUpperCase()) {
            case "₹", "INR", "RS", "RS." -> "INR";
            case "$", "USD" -> "USD";
            case "€", "EUR" -> "EUR";
            case "£", "GBP" -> "GBP";
            default -> c.toUpperCase();
        };
    }

    private String matchFirst(Pattern p, String text) {
        Matcher m = p.matcher(text); return m.find() ? m.group() : null;
    }

    private List<LineItem> extractLineItems(String text) {
        List<LineItem> items = new ArrayList<>();
        for (String line : text.split("\\R")) {
            Matcher m = Pattern.compile("^(.{3,60}?)\\s+([0-9,]+\\.?[0-9]{0,2})\\s*$").matcher(line.trim());
            if (m.matches()) {
                try {
                    items.add(new LineItem(m.group(1).trim(),
                            new BigDecimal(m.group(2).replace(",", ""))));
                } catch (NumberFormatException ignored) {}
            }
            if (items.size() > 40) break;
        }
        return items;
    }

    public record OcrResult(String merchant, BigDecimal totalAmount, LocalDate receiptDate,
                            String currency, String gstin, List<LineItem> items, String rawText) {
        public static OcrResult empty() { return new OcrResult(null, null, null, null, null, List.of(), ""); }
    }
    public record LineItem(String description, BigDecimal amount) {}
}
