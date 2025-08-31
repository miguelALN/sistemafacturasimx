package com.bancomext.web.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class UtileriasReportesExcel {

  private static final Logger log = LogManager.getLogger(UtileriasReportesExcel.class);

  public static String valorStringObjeto(final Object objeto) {
    String retorno;
    DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    DecimalFormat decimalFormat = new DecimalFormat("###,###.00");

    if (objeto instanceof java.lang.String)
      retorno = (String) objeto;
    else if (objeto instanceof java.lang.Integer)
      retorno = ((Integer) objeto).toString();
    else if (objeto instanceof java.lang.Long)
      retorno = ((Long) objeto).toString();
    else if (objeto instanceof java.lang.Short)
      retorno = ((Short) objeto).toString();
    else if (objeto instanceof java.util.Date)
      retorno = df.format((Date) objeto);
    else if (objeto instanceof java.math.BigDecimal)
      retorno = decimalFormat.format(((BigDecimal) objeto).setScale(4, RoundingMode.DOWN).doubleValue());
    else if (objeto instanceof java.lang.Float)
      retorno = decimalFormat.format(((Float) objeto).doubleValue());
    else if (objeto instanceof java.lang.Double)
      retorno = decimalFormat.format(objeto);
    else if (objeto instanceof java.lang.Character)
      retorno = ((Character) objeto).toString();
    else if (objeto instanceof java.lang.Boolean)
      retorno = ((Boolean) objeto) ? "1" : "0";
    else
      retorno = objeto.toString();

    return retorno;
  }

  public static void HeaderCellStyle(final CellStyle cellStyle, final Font font) {
    cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
    cellStyle.setFillPattern(HSSFCellStyle.SPARSE_DOTS);
    cellStyle.setBorderBottom(HSSFCellStyle.BORDER_DOUBLE);
    cellStyle.setBorderLeft(HSSFCellStyle.BORDER_DOUBLE);
    cellStyle.setBorderRight(HSSFCellStyle.BORDER_DOUBLE);
    cellStyle.setBorderTop(HSSFCellStyle.BORDER_DOUBLE);

    cellStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
    cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

    font.setFontName(HSSFFont.FONT_ARIAL);
    font.setFontHeightInPoints((short) 14);
    font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
    font.setColor(HSSFColor.BLACK.index);
  }

  public static void ContentCellStyle(final CellStyle cellStyle, final Font font) {

    cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
    cellStyle.setFillPattern(HSSFCellStyle.SPARSE_DOTS);
    cellStyle.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
    cellStyle.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
    cellStyle.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
    cellStyle.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
    cellStyle.setFillForegroundColor(HSSFColor.WHITE.index);
    cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

    font.setFontName(HSSFFont.FONT_ARIAL);
    font.setFontHeightInPoints((short) 9);
    //font.setBoldweight(HSSFFont.);
    font.setColor(HSSFColor.BLACK.index);
    cellStyle.setFont(font);
  }

  public static String borrarExportsExistentes(final String filePath, final String nombreArchivoOri) {
    final StringBuilder nombreArchivo = new StringBuilder(nombreArchivoOri);
    File archivo = new File(filePath + nombreArchivo + ".xls");
    for (int i = 0; archivo.exists(); i++) {
      final boolean pudoBorrar = archivo.delete();
      if (!pudoBorrar) {
        nombreArchivo.append(i);
        archivo = new File(filePath + nombreArchivo + ".xls");
      }
    }
    return nombreArchivo.toString();
  }

  public File generaTablaExcelArchivo(final String path,
                                      final String nombreArchivo,
                                      final String sheetName,
                                      final Integer rowInicial,
                                      final List<String> headers,
                                      final List<String> listaNombresColumnas,
                                      final List<Object> listacontenido) {

    final String fileName = borrarExportsExistentes(path, nombreArchivo);
    final File archivo = new File(path + fileName + ".xls");


    try (final HSSFWorkbook wb = new HSSFWorkbook()) {
      final CellStyle styleHeader = wb.createCellStyle();
      final Font fontHeader = wb.createFont();
      HeaderCellStyle(styleHeader, fontHeader);
      styleHeader.setFont(fontHeader);
      final CellStyle styleContent = wb.createCellStyle();
      final Font fontContent = wb.createFont();
      ContentCellStyle(styleContent, fontContent);
      styleContent.setFont(fontContent);
      final HSSFSheet sheet = wb.createSheet(sheetName);
      sheet.setDefaultColumnWidth(30);

      //Genera Headers
      int i = 0;
      HSSFRow headerRow = sheet.createRow(rowInicial);
      for (String header : headers) {
        if (i >= headers.size()) break;
        headerRow.createCell(i).setCellValue(header);
        headerRow.getCell(i).setCellStyle(styleHeader);
        i++;
      }

      HSSFSheet sheet1 = null;
      final int limiteRegistrosPorPestana = 32356;

      if (listacontenido.size() > limiteRegistrosPorPestana &&
          listacontenido.size() <= (limiteRegistrosPorPestana * 2)) {
        sheet1 = wb.createSheet(sheetName + "_1");
        sheet1.setDefaultColumnWidth(30);
        i = 0;
        headerRow = sheet1.createRow(rowInicial);  // row0
        for (String header : headers) {
          if (i >= headers.size()) break;
          headerRow.createCell(i).setCellValue(header);
          headerRow.getCell(i).setCellStyle(styleHeader);
          i++;
        }
      }

      HSSFSheet sheet2 = null;
      if (listacontenido.size() > (limiteRegistrosPorPestana * 2) &&
          listacontenido.size() <= (limiteRegistrosPorPestana * 3)) {
        sheet2 = wb.createSheet(sheetName + "_2");
        sheet2.setDefaultColumnWidth(30);
        i = 0;
        headerRow = sheet2.createRow(rowInicial);  // row0
        for (String header : headers) {
          if (i >= headers.size()) break;
          headerRow.createCell(i).setCellValue(header);
          headerRow.getCell(i).setCellStyle(styleHeader);
          i++;
        }
      }

      HSSFSheet sheet3 = null;
      if (listacontenido.size() > (limiteRegistrosPorPestana * 3) &&
          listacontenido.size() <= (limiteRegistrosPorPestana * 4)) {
        sheet3 = wb.createSheet(sheetName + "_3");
        sheet3.setDefaultColumnWidth(30);
        i = 0;
        headerRow = sheet3.createRow(rowInicial);  // row0
        for (String header : headers) {
          if (i >= headers.size()) break;
          headerRow.createCell(i).setCellValue(header);
          headerRow.getCell(i).setCellStyle(styleHeader);
          i++;
        }
      }

      boolean k = archivo.setWritable(true);
      log.info(k + "El archivo generado es " + archivo.getName());

      HSSFRow rowContent = null;
      int r = rowInicial + 1;
      int r1 = rowInicial + 1;
      int r2 = rowInicial + 1;
      int r3 = rowInicial + 1;


      //genera Contenido
      for (Object bean : listacontenido) {
        PropertyDescriptor[] pd = Introspector.getBeanInfo(bean.getClass()).getPropertyDescriptors();
        if (r <= limiteRegistrosPorPestana) {
          rowContent = sheet.createRow(r);
        }
        if (r > limiteRegistrosPorPestana && r <= (limiteRegistrosPorPestana * 2)) {
          if (sheet1 != null) {
            rowContent = sheet1.createRow(r1);
            r1++;
          }
        }
        if (r > (limiteRegistrosPorPestana * 2) && r <= (limiteRegistrosPorPestana * 3)) {
          if (sheet2 != null) {
            rowContent = sheet2.createRow(r2);
            r2++;
          }
        }
        if (r > (limiteRegistrosPorPestana * 3) && r <= (limiteRegistrosPorPestana * 4)) {
          if (sheet3 != null) {
            rowContent = sheet3.createRow(r3);
            r3++;
          }
        }
        r++;

        int col = 0;
        for (String columnas : listaNombresColumnas) {
          for (int cont = 0; cont <= pd.length - 1; cont++) {
            if (columnas.equals(pd[cont].getName())) {
              if (rowContent != null) {
                rowContent.createCell(col).setCellType(Cell.CELL_TYPE_STRING);
                rowContent.createCell(col).setCellValue(pd[cont].getReadMethod().invoke(bean) == null ? "" :
                    valorStringObjeto((pd[cont].getReadMethod().invoke(bean))));
                rowContent.getCell(col).setCellStyle(styleContent);
                col++;
              }
            }
          }
        }
      }
      try (FileOutputStream out = new FileOutputStream(archivo)) {
        wb.write(out);
      }
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }

    return archivo;
  }

  public void generaTablaExcel(final HSSFWorkbook wb,
                               final HSSFSheet sheet,
                               final Integer rowInicial,
                               final List<String> headers,
                               final List<String> listaNombresColumnas,
                               final List<Object> listacontenido) {
    try {

      final CellStyle styleHeader = wb.createCellStyle();
      final CellStyle styleContent = wb.createCellStyle();
      Font fontHeader = wb.createFont();
      Font fontContent = wb.createFont();
      HeaderCellStyle(styleHeader, fontHeader);
      styleHeader.setFont(fontHeader);
      ContentCellStyle(styleContent, fontContent);
      styleContent.setFont(fontContent);
      sheet.setDefaultColumnWidth(30);

      //Genera Headers
      int i = 0;
      HSSFRow headerRow = sheet.createRow(rowInicial);  // row0
      for (String header : headers) {
        if (i >= headers.size()) break;
        headerRow.createCell(i).setCellValue(header);
        headerRow.getCell(i).setCellStyle(styleHeader);
        i++;
      }

      //genera Contenido
      int r = rowInicial + 1;
      for (Object bean : listacontenido) {
        HSSFRow rowContent = sheet.createRow(r);
        PropertyDescriptor[] pd = Introspector.getBeanInfo(bean.getClass()).getPropertyDescriptors();
        int col = 0;
        for (String columnas : listaNombresColumnas) {
          for (int cont = 0; cont <= pd.length - 1; cont++) {
            if (columnas.contains(".")) {
              for (int contA = 0; contA <= pd.length - 1; contA++) {
                if (columnas.split("\\.")[0].equals(pd[contA].getName())) {
                  Object bean2 = pd[contA].getReadMethod().invoke(bean);
                  PropertyDescriptor[] pd2 = Introspector.getBeanInfo(bean2.getClass()).getPropertyDescriptors();
                  for (int cont2 = 0; cont2 <= pd2.length - 1; cont2++) {
                    if (columnas.split("\\.")[1].equals(pd2[cont2].getName())) {
                      rowContent.createCell(col).setCellType(Cell.CELL_TYPE_STRING);
                      rowContent.createCell(col).setCellValue(
                          pd2[cont2].getReadMethod().invoke(bean2) == null ? "" :
                              valorStringObjeto((pd2[cont2].getReadMethod().invoke(bean2)))
                      );
                      rowContent.getCell(col).setCellStyle(styleContent);
                    }
                  }
                }
              }
            } else if (columnas.equals(pd[cont].getName())) {
              rowContent.createCell(col).setCellType(Cell.CELL_TYPE_STRING);
              rowContent.createCell(col).setCellValue(
                  pd[cont].getReadMethod().invoke(bean) == null ? "" :
                      valorStringObjeto((pd[cont].getReadMethod().invoke(bean)))
              );
              rowContent.getCell(col).setCellStyle(styleContent);
            }
          }
          col++;
        }
        r++;
      }
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
  }

}
