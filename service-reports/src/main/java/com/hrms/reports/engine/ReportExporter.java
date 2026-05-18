package com.hrms.reports.engine;

import com.hrms.pdf.service.PdfService;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ReportExporter {

    private final PdfService pdfService;

    public byte[] toCsv(ReportQueryEngine.ResultSet rs) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             CSVWriter w = new CSVWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            w.writeNext(rs.columns().toArray(String[]::new));
            for (Map<String, Object> r : rs.rows()) {
                String[] row = new String[rs.columns().size()];
                for (int i = 0; i < rs.columns().size(); i++) {
                    Object v = r.get(rs.columns().get(i));
                    row[i] = v == null ? "" : v.toString();
                }
                w.writeNext(row);
            }
            w.flush();
            return baos.toByteArray();
        } catch (Exception e) { throw new RuntimeException("CSV export failed", e); }
    }

    public byte[] toExcel(String sheetName, ReportQueryEngine.ResultSet rs) {
        try (SXSSFWorkbook wb = new SXSSFWorkbook(500);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet(sheetName == null ? "Report" : sheetName);
            Row header = sheet.createRow(0);
            CellStyle headerStyle = wb.createCellStyle();
            Font bold = wb.createFont(); bold.setBold(true);
            headerStyle.setFont(bold);
            for (int i = 0; i < rs.columns().size(); i++) {
                Cell c = header.createCell(i);
                c.setCellValue(rs.columns().get(i));
                c.setCellStyle(headerStyle);
            }
            int rowIdx = 1;
            for (Map<String, Object> r : rs.rows()) {
                Row out = sheet.createRow(rowIdx++);
                for (int i = 0; i < rs.columns().size(); i++) {
                    Object v = r.get(rs.columns().get(i));
                    Cell c = out.createCell(i);
                    if (v instanceof Number n) c.setCellValue(n.doubleValue());
                    else if (v instanceof Boolean b) c.setCellValue(b);
                    else c.setCellValue(v == null ? "" : v.toString());
                }
            }
            wb.write(baos);
            wb.dispose();
            return baos.toByteArray();
        } catch (Exception e) { throw new RuntimeException("Excel export failed", e); }
    }

    public byte[] toPdf(String title, ReportQueryEngine.ResultSet rs) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family:Helvetica,Arial,sans-serif;font-size:9px'>")
            .append("<h2>").append(title == null ? "Report" : title).append("</h2>")
            .append("<table style='border-collapse:collapse;width:100%;border:1px solid #ccc'>");
        html.append("<tr style='background:#f3f4f6'>");
        for (String c : rs.columns()) html.append("<th align='left'>").append(c).append("</th>");
        html.append("</tr>");
        for (Map<String, Object> r : rs.rows()) {
            html.append("<tr>");
            for (String col : rs.columns()) {
                Object v = r.get(col);
                html.append("<td>").append(v == null ? "" : v.toString()).append("</td>");
            }
            html.append("</tr>");
        }
        html.append("</table></body></html>");
        return pdfService.htmlToPdf(html.toString());
    }
}
