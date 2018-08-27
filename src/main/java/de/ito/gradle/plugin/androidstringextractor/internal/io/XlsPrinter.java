package de.ito.gradle.plugin.androidstringextractor.internal.io;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.SheetUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.io.IOException;
import java.io.OutputStream;
import java.text.AttributedString;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Logger;

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

    boolean first = true;
    for (Iterator<Row> it = sheet.rowIterator(); it.hasNext(); ) {
      Row row = it.next();
      if (first) {
        first = false;
      } else {
        autoHeightRow(row);
      }
    }

    workbook.write(outputStream);
  }

  private static Logger logger = Logger.getAnonymousLogger();

  private void autoHeightRow(Row row) {
    double height = getRowHeight(row);

    if (height != -1) {
      height *= 20;
      if (height > 0xfffff) {
        height = 0xfffff;
      }
      row.setHeight((short) (height));
    }
  }

  private double getRowHeight(Row row) {
    int defaultCharHeight = getDefaultCharHeight();

    double height = -1;
    for (int c = 0, max = columnCount; c <= max; c++) {
      Cell cell = row.getCell(c);
      if (cell != null) {
        double cellHeight = getCellHeight(cell, defaultCharHeight);
        height = Math.max(height, cellHeight);
      }
    }
    return height;
  }

  /**
   * drawing context to measure text
   */
  private static final FontRenderContext fontRenderContext = new FontRenderContext(null, true, true);

  /**
   * Excel measures columns in units of 1/256th of a character width
   * but the docs say nothing about what particular character is used.
   * '0' looks to be a good choice.
   */
  private static final char DEFAULT_CHAR = '|';

  /**
   * This is the multiple that the font height is scaled by when determining the
   * boundary of rotated text.
   */
  private static final double fontHeightMultiple = 2.0;

  /**
   * Copy text attributes from the supplied Font to Java2D AttributedString
   */
  private int getDefaultCharHeight() {
    Font font = workbook.getFontAt((short) 0);
    int startIdx = 0;
    int endIdx = 1;
    AttributedString str = new AttributedString(String.valueOf(DEFAULT_CHAR));

    // Copy attributes
    copyAttributes(font, str, startIdx, endIdx);

    TextLayout layout = new TextLayout(str.getIterator(), fontRenderContext);
    return (int) (layout.getAscent() + layout.getDescent());
  }

  /**
   * Compute width of a single cell
   *
   * @param cell              the cell whose width is to be calculated
   * @param defaultCharHeight the width of a single character
   * @return the width in pixels or -1 if cell is empty
   */
  private static double getCellHeight(Cell cell, int defaultCharHeight) {
    Sheet sheet = cell.getSheet();
    Workbook wb = sheet.getWorkbook();
    Row row = cell.getRow();
    int column = cell.getColumnIndex();

    // FIXME: this looks very similar to getCellWithMerges below. Consider consolidating.
    // We should only be checking merged regions if useMergedCells is true. Why are we doing this for-loop?
    for (CellRangeAddress region : sheet.getMergedRegions()) {
      if (SheetUtil.containsCell(region, row.getRowNum(), column)) {
        // If we're not using merged cells, skip this one and move on to the next.
        return -1;
      }
    }

    float columnWidth = sheet.getColumnWidthInPixels(column);

    CellStyle style = cell.getCellStyle();
    CellType cellType = cell.getCellTypeEnum();

    // for formula cells we compute the cell width for the cached formula result
    if (cellType == CellType.FORMULA)
      cellType = cell.getCachedFormulaResultTypeEnum();

    Font font = wb.getFontAt(style.getFontIndex());

    double height = -1;
    if (cellType == CellType.STRING) {
      RichTextString rt = cell.getRichStringCellValue();
      String[] lines = rt.getString().split("\\n");
      for (String line : lines) {
        if (line.isEmpty()) {
          continue;
        }

        AttributedString str = new AttributedString(line);
        copyAttributes(font, str, 0, line.length());

//        if (rt.numFormattingRuns() > 0) {
//          // TODO: support rich text fragments
//        }

        height = getCellHeight(defaultCharHeight, columnWidth, style, height, str, line);
      }
    } else {
      String sval = null;
      if (cellType == CellType.NUMERIC) {
        // Try to get it formatted to look the same as excel
//        try {
//          sval = formatter.formatCellValue(cell, dummyEvaluator);
//        } catch (Exception e) {
        sval = String.valueOf(cell.getNumericCellValue());
//        }
      } else if (cellType == CellType.BOOLEAN) {
        sval = String.valueOf(cell.getBooleanCellValue()).toUpperCase(Locale.ROOT);
      }
      if (sval != null && !sval.isEmpty()) {
        AttributedString str = new AttributedString(sval);
        copyAttributes(font, str, 0, sval.length());

        height = getCellHeight(defaultCharHeight, columnWidth, style, height, str, sval);
      }
    }
    return height;
  }

  /**
   * Calculate the best-fit width for a cell
   * If a merged cell spans multiple columns, evenly distribute the column width among those columns
   *
   * @param defaultCharHeight the width of a character using the default font in a workbook
   * @param style             the cell style, which contains text rotation and indention information needed to compute the cell width
   * @param minHeight         the minimum best-fit width. This algorithm will only return values greater than or equal to the minimum width.
   * @param str               the text contained in the cell
   * @return the best fit cell width
   */
  private static double getCellHeight(int defaultCharHeight, float columnWidth, CellStyle style,
                                      double minHeight, AttributedString str, String txt) {

    LineBreakMeasurer measurer = new LineBreakMeasurer(str.getIterator(), fontRenderContext);

    int lineCount = 0;
    while (measurer.getPosition() < txt.length()) {
      int nextPos = measurer.nextOffset(columnWidth);
      lineCount++;
      measurer.setPosition(nextPos);
    }

    return Math.max(minHeight, lineCount * defaultCharHeight * 1.2);

//    TextLayout layout = new TextLayout(str.getIterator(), fontRenderContext);
//    final Rectangle2D bounds;
//    if (style.getRotation() != 0) {
//      /*
//       * Transform the text using a scale so that it's height is increased by a multiple of the leading,
//       * and then rotate the text before computing the bounds. The scale results in some whitespace around
//       * the unrotated top and bottom of the text that normally wouldn't be present if unscaled, but
//       * is added by the standard Excel autosize.
//       */
//      AffineTransform trans = new AffineTransform();
//      trans.concatenate(AffineTransform.getRotateInstance(style.getRotation() * 2.0 * Math.PI / 360.0));
//      trans.concatenate(AffineTransform.getScaleInstance(1, fontHeightMultiple));
//      bounds = layout.getOutline(trans).getBounds();
//    } else {
//      bounds = layout.getBounds();
//    }
//    // frameHeight accounts for leading spaces which is excluded from bounds.getHeight()
//    final double frameHeight = bounds.getY() + bounds.getHeight();
//    return Math.max(minHeight, (frameHeight / defaultCharHeight));// + style.getIndention());
  }

  /**
   * Copy text attributes from the supplied Font to Java2D AttributedString
   */
  private static void copyAttributes(Font font, AttributedString str, int startIdx, int endIdx) {
    str.addAttribute(TextAttribute.FAMILY, font.getFontName(), startIdx, endIdx);
    str.addAttribute(TextAttribute.SIZE, (float) font.getFontHeightInPoints());
    if (font.getBoldweight() == Font.BOLDWEIGHT_BOLD) {
      str.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, startIdx, endIdx);
    }
    if (font.getItalic()) {
      str.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE, startIdx, endIdx);
    }
    if (font.getUnderline() == Font.U_SINGLE) {
      str.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, startIdx, endIdx);
    }
  }
}
