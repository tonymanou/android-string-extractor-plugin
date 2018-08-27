package de.ito.gradle.plugin.androidstringextractor.internal.io;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;

public class XlsPrinter implements Printer {

  private final XSSFWorkbook workbook = new XSSFWorkbook();
  private final OutputStream outputStream;

  private CellStyle baseStyle = workbook.createCellStyle();
  private XSSFSheet sheet = workbook.createSheet("Datatypes in Java");

  private int rowNum = 0;
  private int columnCount = 0;

  public XlsPrinter(OutputStream out) {
//    baseStyle.setAlignment(HorizontalAlignment.JUSTIFY);
    baseStyle.setWrapText(true);
    outputStream = out;
  }

  @Override
  public void addHeaderRow(String[] columns) {
    columnCount = Math.max(columnCount, columns.length);

    Font font = workbook.createFont();
    font.setBold(true);
    CellStyle style = workbook.createCellStyle();
    style.setFont(font);

    Row row = sheet.createRow(rowNum++);
    for (int i = 0; i < columns.length; i++) {
      Cell cell = row.createCell(i);
      cell.setCellStyle(style);
      cell.setCellValue(columns[i]);
    }
  }

  @Override
  public void addRow(String[] columns) {
    columnCount = Math.max(columnCount, columns.length);

    Row row = sheet.createRow(rowNum++);
    for (int i = 0; i < columns.length; i++) {
      Cell cell = row.createCell(i);
      if (i != 0) {
        cell.setCellStyle(baseStyle);
      }
      cell.setCellValue(columns[i]);
    }
  }

  @Override
  public void writeToDisk() throws IOException {
    Font font = workbook.createFont();
    font.setItalic(true);
    CellStyle style = workbook.createCellStyle();
    style.setFont(font);

    for (int i = 0, max = columnCount; i < max; i++) {
      if (i == 0) {
        sheet.setDefaultColumnStyle(i, style);
        sheet.autoSizeColumn(i);
      } else {
        sheet.setColumnWidth(i, 50 * 256);
      }
    }

    workbook.write(outputStream);
  }
}
