package com.hrms.pdf.service;

import com.lowagie.text.*;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class OpenPdfService implements PdfService {

    private static final Pattern VAR = Pattern.compile("\\{\\{\\s*([\\w.]+)\\s*}}");

    @Override
    public byte[] htmlToPdf(String html) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(doc, out);
            doc.open();
            StyleSheet css = new StyleSheet();
            css.loadTagStyle("body", "font-family", "Helvetica");
            css.loadTagStyle("body", "font-size", "10");
            HTMLWorker hw = new HTMLWorker(doc);
            hw.setStyleSheet(css);
            hw.parse(new StringReader(html));
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF render failed: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] renderTemplate(String template, Map<String, Object> vars) {
        Matcher m = VAR.matcher(template == null ? "" : template);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            Object v = (vars == null) ? null : vars.get(m.group(1));
            m.appendReplacement(sb, Matcher.quoteReplacement(v == null ? "" : v.toString()));
        }
        m.appendTail(sb);
        return htmlToPdf(sb.toString());
    }

    @Override
    public byte[] watermark(byte[] pdf, String text) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfReader reader = new PdfReader(pdf);
            PdfStamper stamper = new PdfStamper(reader, out);
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, false);
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                PdfContentByte over = stamper.getOverContent(i);
                PdfGState g = new PdfGState(); g.setFillOpacity(0.18f);
                over.saveState(); over.setGState(g);
                over.beginText(); over.setFontAndSize(bf, 60); over.setColorFill(Color.GRAY);
                Rectangle p = reader.getPageSize(i);
                over.showTextAligned(Element.ALIGN_CENTER, text, p.getWidth()/2, p.getHeight()/2, 45);
                over.endText(); over.restoreState();
            }
            stamper.close(); reader.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Watermark failed: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] signaturePlaceholder(byte[] pdf, String fieldName) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfReader reader = new PdfReader(pdf);
            PdfStamper stamper = PdfStamper.createSignature(reader, out, '\0');
            PdfSignatureAppearance sa = stamper.getSignatureAppearance();
            sa.setReason("Approved");
            sa.setLocation("HRMS");
            sa.setVisibleSignature(new Rectangle(40, 40, 240, 100),
                    reader.getNumberOfPages(), fieldName == null ? "sig1" : fieldName);
            stamper.close(); reader.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Signature placeholder failed: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] encrypt(byte[] pdf, String ownerPassword, String userPassword) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfReader reader = new PdfReader(pdf);
            PdfStamper stamper = new PdfStamper(reader, out);
            stamper.setEncryption(
                    userPassword == null ? null : userPassword.getBytes(),
                    ownerPassword.getBytes(),
                    PdfWriter.ALLOW_PRINTING,
                    PdfWriter.ENCRYPTION_AES_128);
            stamper.close(); reader.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Encrypt failed: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] stampHeaderFooter(byte[] pdf, String header, String footer) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfReader reader = new PdfReader(pdf);
            PdfStamper stamper = new PdfStamper(reader, out);
            BaseFont bf = BaseFont.createFont();
            int n = reader.getNumberOfPages();
            for (int i = 1; i <= n; i++) {
                PdfContentByte cb = stamper.getOverContent(i);
                cb.beginText(); cb.setFontAndSize(bf, 8); cb.setColorFill(Color.DARK_GRAY);
                Rectangle p = reader.getPageSize(i);
                if (header != null) cb.showTextAligned(Element.ALIGN_LEFT, header, 40, p.getHeight() - 20, 0);
                String foot = (footer == null ? "" : footer) + "   Page " + i + "/" + n;
                cb.showTextAligned(Element.ALIGN_RIGHT, foot, p.getWidth() - 40, 20, 0);
                cb.endText();
            }
            stamper.close(); reader.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Header/footer stamp failed: " + e.getMessage(), e);
        }
    }
}
