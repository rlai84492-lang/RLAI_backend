package com.example.titan_watch_learning_project.serviceImpl;

import com.example.titan_watch_learning_project.entity.Lead;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Leads ki list ko Excel (.xlsx) file mein convert karta hai.
 * Email attachment ke liye byte array return karta hai.
 */
@Slf4j
@Service
public class LeadExcelReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    private static final String[] HEADERS = {
            "#", "Name", "Phone", "Type", "Flow",
            "Collection", "Brand", "Step", "Status", "Created"
    };

    /**
     * Leads ko Excel byte array mein convert karta hai.
     * Email attachment ke liye use hota hai.
     */
    public byte[] generateExcel(List<Lead> leads, String sheetTitle) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet(sheetTitle);

            // ── Header row style ──────────────────────────────
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // ── Header row likho ───────────────────────────────
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── Data rows likho ────────────────────────────────
            int rowNum = 1;
            for (Lead lead : leads) {
                Row row = sheet.createRow(rowNum);

                row.createCell(0).setCellValue(rowNum);
                row.createCell(1).setCellValue(defaultIfBlank(lead.getCustomerName()));
                row.createCell(2).setCellValue(defaultIfBlank(lead.getPhone()));
                row.createCell(3).setCellValue(formatLeadType(lead.getLeadType()));
                row.createCell(4).setCellValue(defaultIfBlank(lead.getFlow()));
                row.createCell(5).setCellValue(defaultIfBlank(lead.getSelectedCollection()));
                row.createCell(6).setCellValue(formatUnderscore(lead.getSelectedBrand()));
                row.createCell(7).setCellValue(formatUnderscore(lead.getStepName()));
                row.createCell(8).setCellValue(lead.getStatus() != null ? lead.getStatus().name() : "—");
                row.createCell(9).setCellValue(lead.getCreatedAt() != null
                        ? lead.getCreatedAt().format(DATE_FMT) : "—");

                rowNum++;
            }

            // ── Column widths auto-size karo ───────────────────
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    private String defaultIfBlank(String value) {
        return (value == null || value.isBlank()) ? "—" : value;
    }

    private String formatUnderscore(String value) {
        if (value == null || value.isBlank()) return "—";
        return value.replace("_", " ");
    }

    private String formatLeadType(Lead.LeadType leadType) {
        if (leadType == null) return "—";
        return switch (leadType) {
            case CALLBACK    -> "Callback";
            case STORE_VISIT -> "Store Visit";
            case WEBSITE     -> "Website";
        };
    }
}