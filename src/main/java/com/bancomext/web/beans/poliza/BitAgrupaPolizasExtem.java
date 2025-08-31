package com.bancomext.web.beans.poliza;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiBitacoraExtemporImx;
import com.bancomext.service.mapping.VoLlaveValor;
import com.bancomext.web.utils.Constantes;
import com.bancomext.web.utils.Utilerias;
import com.bancomext.web.utils.UtileriasReportesExcel;
import com.bancomext.web.validator.ValidadorSesion;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.primefaces.model.StreamedContent;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.model.SelectItem;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@ManagedBean(name = "bitAgrupaPolizasExtem")
@ViewScoped
public class BitAgrupaPolizasExtem implements Serializable {

  private static final Logger log = LogManager.getLogger(BitAgrupaPolizasExtem.class);
  private static final String filePath = "IMX/exportar/";
  private final GenericService genericService = ServiceLocator.getGenericService();
  private String nombreArchivo = "BitacoraAgrupacionPolizasExtem";
  private List<FiBitacoraExtemporImx> listaBitacora;
  private List<SelectItem> listaCalendar = new ArrayList<>();
  private UsuarioDTO usuarioLogueado;
  private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
  private String filtroCalendar;
  private UIComponent btnError;
  private UIComponent btnMensaje;
  private boolean paginatorVisible;
  private int defaultRows = Constantes.REGISTROS;
  private StreamedContent excelResource;
  private Utilerias utilerias;

  public BitAgrupaPolizasExtem() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("AgrupacionPolizasExtemporaneo", usuarioLogueado.getRol());
    }
    init();
  }

  @SuppressWarnings("unchecked")
  private void init() {
    excelResource = null;
    listaCalendar = Utilerias.creaSelectItem(Utilerias.getComboMesAnio());

    final Date fProc = ConsultasService.getMaxDate("FI_BITACORA_EXTEMPOR_IMX", "FECHA_PROCESO");
    final String fechaProceso = formatter.format(fProc);

    listaBitacora = genericService.get("FiBitacoraExtemporImx", " TRUNC(FECHA_PROCESO) = TO_DATE('" +
        fechaProceso + "','dd/mm/yyyy')", " ORDER BY OPCODE, OBSRV2, NUMPOL");

    if (listaBitacora != null) {
      for (FiBitacoraExtemporImx reg : listaBitacora) {
        if (reg.getAbono() != null && reg.getCargo() != null) {
          reg.setAbonoFor(Utilerias.formatearNumero(reg.getAbono(), ""));
          reg.setCargoFor(Utilerias.formatearNumero(reg.getCargo(), ""));
          reg.setDiferenciaFor(Utilerias.formatearNumero(reg.getDiferencia(), ""));
          reg.setFechaProcesoFor(formatter.format(reg.getFechaProceso()));
        }
      }
    }

    paginatorVisible = (listaBitacora != null && listaBitacora.size() > 16);
    if (listaBitacora != null && !listaBitacora.isEmpty()) {
      exportarBitacoraCancelacion();
    }
  }

  @SuppressWarnings("unchecked")
  public void filtroBitacora() {

    if (filtroCalendar != null && !filtroCalendar.equals("-1")) {

      String mes = filtroCalendar.substring(0, 2);
      String anio = filtroCalendar.substring(3, 7);

      setListaBitacora(genericService.get("FiBitacoraExtemporImx", " FECAA = " +
          Integer.valueOf(anio) + " AND FECAM = " + Integer.valueOf(mes), " ORDER BY OPCODE, OBSRV2, NUMPOL"));

      if (listaBitacora != null) {
        for (FiBitacoraExtemporImx reg : listaBitacora) {
          if (reg.getAbono() != null && reg.getCargo() != null) {
            reg.setAbonoFor(Utilerias.formatearNumero(reg.getAbono(), ""));
            reg.setCargoFor(Utilerias.formatearNumero(reg.getCargo(), ""));
            reg.setDiferenciaFor(Utilerias.formatearNumero(reg.getDiferencia(), ""));
            reg.setFechaProcesoFor(formatter.format(reg.getFechaProceso()));
          }
        }
      }
    }
    setPaginatorVisible(listaBitacora != null && listaBitacora.size() > 16);
  }

  public void exportarBitacoraCancelacion() {
    log.info("Inicia Exportacion Archivo Excel");

    nombreArchivo = UtileriasReportesExcel.borrarExportsExistentes(filePath, nombreArchivo);
    final File archivo = new File(filePath + nombreArchivo + ".xls");
    archivo.setWritable(true);

    try (final HSSFWorkbook wb = new HSSFWorkbook()) {
      final HSSFFont font = wb.createFont();
      font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

      final HSSFCellStyle style = wb.createCellStyle();
      style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
      style.setFont(font);

      final HSSFSheet sheet = wb.createSheet();
      final HSSFRow rowTit = sheet.createRow((short) 0);

      final HSSFCell cell = rowTit.createCell(0);
      sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));
      cell.setCellValue("BITACORA AGRUPACION POLIZAS EXTEMPORANEO");
      cell.setCellStyle(style);

      final HSSFRow row = sheet.createRow((short) 1);
      row.createCell(0).setCellValue("Codigo de Operacion");
      row.createCell(1).setCellValue("Descripcion");
      row.createCell(2).setCellValue("Numero de Poliza");
      row.createCell(3).setCellValue("Cargo");
      row.createCell(4).setCellValue("Abono");
      row.createCell(5).setCellValue("Diferencia");
      row.createCell(6).setCellValue("Fecha del Proceso");

      int k = listaBitacora.size() + 2;
      for (FiBitacoraExtemporImx bitacora : listaBitacora) {
        k--;
        final HSSFRow row2 = sheet.createRow((short) k);
        row2.createCell(0).setCellValue(bitacora.getOpCode());
        row2.createCell(1).setCellValue(bitacora.getObsrv2());
        row2.createCell(2).setCellValue(bitacora.getNumPol());
        row2.createCell(3).setCellValue(Utilerias.formatearNumero(bitacora.getCargo(), ""));
        row2.createCell(4).setCellValue(Utilerias.formatearNumero(bitacora.getAbono(), ""));
        row2.createCell(5).setCellValue(bitacora.getDiferencia().intValue());
        row2.createCell(6).setCellValue(formatter.format(bitacora.getFechaProceso()));
      }
      try (final FileOutputStream out = new FileOutputStream(archivo)) {
        wb.write(out);

        utilerias = new Utilerias();
        excelResource = utilerias.downlodaFile(filePath + nombreArchivo + ".xls", nombreArchivo + ".xls");

      }
    } catch (Exception ex) {
      log.error("Error Creando Archivo Excel " + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("BitacoraAgrupacionPolizasExtem",
          "exportarBitacoraAgrupacionPolizasExtem", ex, "Error Creando Archivo Excel", usuarioLogueado);
    }
    log.info("Termina Exportacion Archivo Excel");
  }

}


