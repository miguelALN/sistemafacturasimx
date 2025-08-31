package com.bancomext.web.beans.bitacora;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiBitacoraImx;
import com.bancomext.web.helper.LecturaArchivosHelper;
import com.bancomext.web.utils.Constantes;
import com.bancomext.web.utils.Utilerias;
import com.bancomext.web.utils.UtileriasReportesExcel;
import com.bancomext.web.validator.ValidadorSesion;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.primefaces.PrimeFaces;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Data
@ManagedBean(name = "bitCapitalizacionInteres")
@ViewScoped
public class BitCapitalizacionInteres implements Serializable {

  private static final Logger log = LogManager.getLogger(BitCapitalizacionInteres.class);
  private static final String filePath = "IMX/exportar/";
  private final GenericService genericService = ServiceLocator.getGenericService();
  private String nombreArchivo = "BitacoraCapitalizacion";

  private List<FiBitacoraImx> listaBitacoraCapitalizacion;
  private List<SelectItem> listaCalendar = new ArrayList<>();
  private List<SelectItem> listaCampoA = new ArrayList<>();
  private UsuarioDTO usuarioLogueado;
  private String filtroCalendar;
  private String filtroCampoA;
  private boolean paginatorVisible;
  private int defaultRows = Constantes.REGISTROS;

  public BitCapitalizacionInteres() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("BitacoraCapitalizacionIntereses", usuarioLogueado.getRol());
    }
    init();
  }

  private static void mostrarMensaje(final String msg, final boolean esError) {
    final FacesMessage mensaje = new FacesMessage();
    mensaje.setSummary(msg);
    mensaje.setDetail("");
    mensaje.setSeverity(esError ? FacesMessage.SEVERITY_ERROR : FacesMessage.SEVERITY_INFO);
    FacesContext.getCurrentInstance().addMessage(null, mensaje);
    PrimeFaces.current().ajax().update(":form1:messages");
  }

  private void init() {
    listaCalendar = Utilerias.creaSelectItem(Utilerias.getComboMesAnio());
    llenarClientes();
    llenarListaBitacoraCapitalizacion();
  }

  public void filtrarTableBitCapitalizacion() {
    if (filtroCalendar == null || filtroCalendar.equals("-1")) {
      mostrarMensaje("Debe seleccionar una fecha para realizar la busqueda.", false);
    }
    llenarListaBitacoraCapitalizacion();
  }

  public void llenarClientes() {
    log.debug("Entramos a llenarClientes()");
    listaCampoA = Utilerias.creaSelectItem(
        ConsultasService.getComboDistinct("FI_BITACORA_IMX", "CAMPOA", "NOM_CLIENTE",
            condicionMesAnio(), "ORDER BY NOM_CLIENTE")
    );
  }

  private String condicionMesAnio() {
    final String mes;
    final String anio;
    if (filtroCalendar != null && !filtroCalendar.equals("-1")) {
      mes = filtroCalendar.substring(0, 2);
      anio = filtroCalendar.substring(3, 7);
    } else {
      mes = Utilerias.getMesActual();
      anio = Utilerias.getAnioActual();
    }
    return " TIPO_PROCESO = 'CAP' AND ATRIBUTO3 = '" + mes + "' AND ATRIBUTO4 = '" + anio + "' ";
  }

  @SuppressWarnings("unchecked")
  private void llenarListaBitacoraCapitalizacion() {
    log.debug("Entramos a llenarListaBitacoraCapitalizacion()");

    listaBitacoraCapitalizacion = genericService.get("FiBitacoraImx",
        condicionMesAnio() + ((filtroCampoA != null && !filtroCampoA.equals("-1")) ?
            " AND CAMPOA = NVL('" + filtroCampoA + "',CAMPOA) )" : ""), " ORDER BY CAMPOA, CUENTA_ORIGEN");

    for (final FiBitacoraImx bit : listaBitacoraCapitalizacion) {
      bit.setMontoCtaOriFor(Utilerias.formatearNumero(bit.getMontoCtaOrigen(), ""));
      bit.setMontoDesFor(Utilerias.formatearNumero(bit.getMontoDestino(), ""));
      bit.setMontoSusFor(Utilerias.formatearNumero(bit.getMontoSust1(), ""));
      bit.setMontoSust2For(Utilerias.formatearNumero(bit.getMontoSust2(), ""));
    }
    paginatorVisible = (listaBitacoraCapitalizacion.size() > 16);
    exportarBitacoraCapitalizacion();
  }

  public void descargarArchivo() {
    LecturaArchivosHelper.descargarExcel(filePath, nombreArchivo);
  }

  public void exportarBitacoraCapitalizacion() {
    nombreArchivo = UtileriasReportesExcel.borrarExportsExistentes(filePath, nombreArchivo);
    final File archivo = new File(filePath + nombreArchivo + ".xls");
    if (archivo.setWritable(true)) {
      log.debug("Archivo creado");
    }
    try (final HSSFWorkbook wb = new HSSFWorkbook()) {
      final HSSFFont font = wb.createFont();
      font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

      final HSSFCellStyle style = wb.createCellStyle();
      style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
      style.setFont(font);

      final HSSFSheet sheet = wb.createSheet();
      final HSSFRow rowTit = sheet.createRow((short) 0);

      final HSSFCell cell = rowTit.createCell(0);
      sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 11));
      cell.setCellValue("BITACORA CAPITALIZACION INTERESES");
      cell.setCellStyle(style);

      final HSSFRow row = sheet.createRow((short) 1);
      row.createCell(0).setCellValue("CampoA");
      row.createCell(1).setCellValue("Nombre Cliente");
      row.createCell(2).setCellValue("Cuenta de Origen Sicav");
      row.createCell(3).setCellValue("Monto de Origen Sicav");
      row.createCell(4).setCellValue("Cuenta Destino");
      row.createCell(5).setCellValue("Monto");
      row.createCell(6).setCellValue("Cuenta Sustitucion");
      row.createCell(7).setCellValue("Monto");
      row.createCell(8).setCellValue("Cuenta Sustitucion");
      row.createCell(9).setCellValue("Monto");
      row.createCell(10).setCellValue("Moneda Sustitucion");

      final DecimalFormat decimalFormat = new DecimalFormat("###,###.00");

      int k = listaBitacoraCapitalizacion.size() + 2;
      for (FiBitacoraImx bitacora : listaBitacoraCapitalizacion) {
        k--;
        final HSSFRow row2 = sheet.createRow((short) k);
        row2.createCell(0).setCellValue(bitacora.getCampoA());
        row2.createCell(1).setCellValue(bitacora.getNomCliente());
        row2.createCell(2).setCellValue(bitacora.getCuentaOrigen());
        row2.createCell(3).setCellValue(bitacora.getMontoCtaOrigen() != null ?
            decimalFormat.format(bitacora.getMontoCtaOrigen()) : "0.0");
        row2.createCell(4).setCellValue(bitacora.getCuentaDestino());
        row2.createCell(5).setCellValue(bitacora.getMontoDestino() != null ?
            decimalFormat.format(bitacora.getMontoDestino()) : "0.0");
        row2.createCell(6).setCellValue(bitacora.getCuentaSust1());
        row2.createCell(7).setCellValue(bitacora.getMontoSust1() != null ?
            decimalFormat.format(bitacora.getMontoSust1()) : "0.0");
        row2.createCell(8).setCellValue(bitacora.getCuentaSust2());
        row2.createCell(9).setCellValue(bitacora.getMontoSust2() != null ?
            decimalFormat.format(bitacora.getMontoSust2()) : "0.0");
        row2.createCell(10).setCellValue(bitacora.getDesMon());
      }
      try (final FileOutputStream out = new FileOutputStream(archivo)) {
        wb.write(out);
      }
    } catch (Exception ex) {
      log.error("Error Creando Archivo Excel " + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("BitacoraCapitalizacion", "exportarBitacoraCapitalizacion", ex,
          "Error Creando Archivo Excel", usuarioLogueado);
    }
  }

}


