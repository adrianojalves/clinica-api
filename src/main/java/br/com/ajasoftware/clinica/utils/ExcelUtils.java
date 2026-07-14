package br.com.ajasoftware.clinica.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Reusable utility class for reading and writing Excel (.xlsx) files.
 * Adheres to SOLID principles by isolating the cell conversion and spreadsheet generation logic
 * from the service classes, allowing code reuse across various report modules.
 */
public final class ExcelUtils {

    private ExcelUtils() {
        // Prevent instantiation
    }

    /**
     * Functional interface to map an entity or DTO to a spreadsheet Row.
     */
    @FunctionalInterface
    public interface RowMapper<T> {
        void map(T item, Row row);
    }

    /**
     * Generates a byte array of an Excel spreadsheet.
     *
     * @param sheetName the name of the sheet
     * @param headers   the header row labels
     * @param items     the list of items to populate
     * @param mapper    the lambda function mapper that maps an item to a Row
     * @param <T>       the item type
     * @return spreadsheet bytes
     */
    public static <T> byte[] exportToExcel(String sheetName, String[] headers, List<T> items, RowMapper<T> mapper) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(sheetName);

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowIdx = 1;
            for (T item : items) {
                Row row = sheet.createRow(rowIdx++);
                mapper.map(item, row);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar arquivo Excel: " + e.getMessage(), e);
        }
    }

    /**
     * Safely reads a cell value as BigDecimal.
     */
    public static BigDecimal getBigDecimalCellValue(Cell cell) {
        if (cell == null) {
            return BigDecimal.ZERO;
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                return BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING:
                try {
                    String strVal = cell.getStringCellValue().trim().replace(",", ".");
                    return strVal.isEmpty() ? BigDecimal.ZERO : new BigDecimal(strVal);
                } catch (NumberFormatException e) {
                    return BigDecimal.ZERO;
                }
            case BLANK:
            default:
                return BigDecimal.ZERO;
        }
    }

    /**
     * Safely reads a cell value as Long.
     */
    public static Long getLongCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                return (long) cell.getNumericCellValue();
            case STRING:
                try {
                    String strVal = cell.getStringCellValue().trim();
                    return strVal.isEmpty() ? null : Long.valueOf(strVal);
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }

    /**
     * Safely reads a cell value as String.
     */
    public static String getStringCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                double num = cell.getNumericCellValue();
                if (num == (long) num) {
                    return String.valueOf((long) num);
                } else {
                    return String.valueOf(num);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
            default:
                return "";
        }
    }
}
