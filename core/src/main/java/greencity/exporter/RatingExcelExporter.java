package greencity.exporter;

import greencity.dto.ratingstatistics.RatingStatisticsDto;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

/**
 * This class is used for export {@link greencity.entity.RatingStatistics} data
 * to Excel file.
 *
 * @author Dovganyuk Taras
 */
@Slf4j
@Component
public class RatingExcelExporter {
    /**
     * Export {@link greencity.entity.RatingStatistics} data to Excel file.
     *
     * @author Dovganyuk Taras
     */
    public void export(OutputStream outputStream, List<RatingStatisticsDto> ratingStatisticsDtoList) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle style = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setBold(true);
            font.setFontHeight(14);
            style.setFont(font);

            XSSFSheet sheet = workbook.createSheet("Rating");
            Row row = sheet.createRow(0);
            setCell(row, 0, style, "Id");
            setCell(row, 1, style, "Event");
            setCell(row, 2, style, "Date");
            setCell(row, 3, style, "UserId");
            setCell(row, 4, style, "User email");
            setCell(row, 5, style, "Points changed");
            setCell(row, 6, style, "Current rating");

            int rowCount = 1;
            for (RatingStatisticsDto dto : ratingStatisticsDtoList) {
                row = sheet.createRow(rowCount++);

                setCell(row, 0, dto.getId().toString(), sheet);
                setCell(row, 1, dto.getRatingPoints().getName(), sheet);
                setCell(row, 2, dto.getCreateDate().toString(), sheet);
                setCell(row, 3, dto.getUser().getId().toString(), sheet);
                setCell(row, 4, dto.getUser().getEmail(), sheet);
                setCell(row, 5, Float.toString(dto.getPointsChanged()), sheet);
                setCell(row, 6, Float.toString(dto.getRating()), sheet);
            }

            workbook.write(outputStream);
        } catch (IOException ex) {
            log.error("Export to excel file error {}", ex.getMessage());
        }
    }

    private static void setCell(Row row, Integer cellNumber, CellStyle style, String cellName) {
        Cell cell = row.createCell(cellNumber);
        cell.setCellStyle(style);
        cell.setCellValue(cellName);
    }

    private static void setCell(Row row, Integer cellNumber, String value, Sheet sheet) {
        Cell cell = row.createCell(cellNumber);
        cell.setCellValue(value);
        sheet.autoSizeColumn(cellNumber);
    }
}
