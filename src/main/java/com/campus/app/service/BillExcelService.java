package com.campus.app.service;

import com.campus.app.dto.BillItemDto;
import com.campus.app.entity.Bill;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Builds the bills spreadsheet. It is regenerated from the database on every
 * export, so the sheet always matches what is saved (no file to get out of sync).
 *
 * Sheet 1 "Bills": one row per bill + a TOTAL row using SUM formulas.
 * Sheet 2 "Line Items": one row per extracted line item.
 */
@Service
@RequiredArgsConstructor
public class BillExcelService {

    private static final String[] BILL_HEADERS = {
            "#", "Bill No", "Vendor", "Bill Date", "Category", "Club",
            "Subtotal", "Tax", "Total", "Currency", "Uploaded By", "Uploaded On", "File", "Confidence %", "Items"
    };
    private static final int[] BILL_WIDTHS = {5, 16, 28, 13, 22, 20, 13, 12, 14, 10, 20, 18, 28, 13, 60};

    private static final String[] ITEM_HEADERS = {"Bill No", "Vendor", "Description", "Qty", "Rate", "Amount"};
    private static final int[] ITEM_WIDTHS = {16, 28, 40, 10, 12, 14};

    private final BillService billService;

    public byte[] build(List<Bill> bills) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Styles st = new Styles(wb);
            buildBillsSheet(wb, st, bills);
            buildItemsSheet(wb, st, bills);
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Could not build the Excel file", e);
        }
    }

    private void buildBillsSheet(Workbook wb, Styles st, List<Bill> bills) {
        Sheet sheet = wb.createSheet("Bills");
        header(sheet, st, BILL_HEADERS, BILL_WIDTHS);

        int r = 1;
        for (Bill b : bills) {
            Row row = sheet.createRow(r);
            int c = 0;
            num(row, c++, r, st.integer);
            text(row, c++, b.getBillNumber());
            text(row, c++, b.getVendor());
            date(row, c++, b.getBillDate(), st.date);
            text(row, c++, b.getCategory());
            text(row, c++, b.getClub() == null ? null : b.getClub().getName());
            money(row, c++, b.getSubtotal() == null ? null : b.getSubtotal().doubleValue(), st.money);
            money(row, c++, b.getTaxAmount() == null ? null : b.getTaxAmount().doubleValue(), st.money);
            money(row, c++, b.getTotalAmount().doubleValue(), st.money);
            text(row, c++, b.getCurrency());
            text(row, c++, b.getUploadedBy().getName());
            dateTime(row, c++, b.getCreatedAt(), st.dateTime);
            text(row, c++, b.getFileName());
            num(row, c++, b.getConfidence() == null ? null : Math.round(b.getConfidence() * 100.0), st.integer);
            text(row, c, itemsSummary(b));
            r++;
        }

        if (!bills.isEmpty()) {
            Row total = sheet.createRow(r);
            Cell label = total.createCell(5);
            label.setCellValue("TOTAL");
            label.setCellStyle(st.boldLabel);
            for (int col : new int[]{6, 7, 8}) {
                String letter = String.valueOf((char) ('A' + col));
                Cell cell = total.createCell(col);
                cell.setCellFormula("SUM(" + letter + "2:" + letter + r + ")");
                cell.setCellStyle(st.moneyBold);
            }
        }

        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new CellRangeAddress(0, Math.max(bills.size(), 1), 0, BILL_HEADERS.length - 1));
    }

    // "Paint (500.00); Box (500.00)" so every item is visible on the main sheet
    private String itemsSummary(Bill b) {
        return billService.readItems(b.getItemsJson()).stream()
                .map(i -> i.amount() == null
                        ? i.description()
                        : i.description() + " (" + String.format(Locale.ROOT, "%.2f", i.amount()) + ")")
                .collect(Collectors.joining("; "));
    }

    private void buildItemsSheet(Workbook wb, Styles st, List<Bill> bills) {
        Sheet sheet = wb.createSheet("Line Items");
        header(sheet, st, ITEM_HEADERS, ITEM_WIDTHS);

        int r = 1;
        for (Bill b : bills) {
            for (BillItemDto item : billService.readItems(b.getItemsJson())) {
                Row row = sheet.createRow(r++);
                text(row, 0, b.getBillNumber());
                text(row, 1, b.getVendor());
                text(row, 2, item.description());
                num(row, 3, item.quantity(), st.plain);
                money(row, 4, item.rate(), st.money);
                money(row, 5, item.amount(), st.money);
            }
        }
        sheet.createFreezePane(0, 1);
    }

    // ---- cell helpers (setCellValue(String) writes text, never a formula,
    //      so a vendor name like "=HYPERLINK(...)" stays harmless text) ----

    private static void header(Sheet sheet, Styles st, String[] headers, int[] widths) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(st.header);
            sheet.setColumnWidth(i, widths[i] * 256);
        }
    }

    private static void text(Row row, int col, String value) {
        if (value != null) row.createCell(col).setCellValue(value);
    }

    private static void num(Row row, int col, Number value, CellStyle style) {
        if (value == null) return;
        Cell cell = row.createCell(col);
        cell.setCellValue(value.doubleValue());
        cell.setCellStyle(style);
    }

    private static void money(Row row, int col, Double value, CellStyle style) {
        num(row, col, value, style);
    }

    private static void date(Row row, int col, LocalDate value, CellStyle style) {
        if (value == null) return;
        Cell cell = row.createCell(col);
        cell.setCellValue(Date.from(value.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        cell.setCellStyle(style);
    }

    private static void dateTime(Row row, int col, LocalDateTime value, CellStyle style) {
        if (value == null) return;
        Cell cell = row.createCell(col);
        cell.setCellValue(Date.from(value.atZone(ZoneId.systemDefault()).toInstant()));
        cell.setCellStyle(style);
    }

    private static final class Styles {
        final CellStyle header, money, moneyBold, boldLabel, date, dateTime, integer, plain;

        Styles(Workbook wb) {
            DataFormat fmt = wb.createDataFormat();
            Font bold = wb.createFont();
            bold.setBold(true);

            header = wb.createCellStyle();
            header.setFont(bold);
            header.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            money = wb.createCellStyle();
            money.setDataFormat(fmt.getFormat("#,##0.00"));

            moneyBold = wb.createCellStyle();
            moneyBold.setDataFormat(fmt.getFormat("#,##0.00"));
            moneyBold.setFont(bold);

            boldLabel = wb.createCellStyle();
            boldLabel.setFont(bold);

            date = wb.createCellStyle();
            date.setDataFormat(fmt.getFormat("dd-mmm-yyyy"));

            dateTime = wb.createCellStyle();
            dateTime.setDataFormat(fmt.getFormat("dd-mmm-yyyy hh:mm"));

            integer = wb.createCellStyle();
            integer.setDataFormat(fmt.getFormat("0"));

            plain = wb.createCellStyle();
        }
    }
}
