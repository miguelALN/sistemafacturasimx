package com.bancomext.web.beans.bitacora;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiBitacoraImx;
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
@ManagedBean(name = "bitReclasificarSaldoNeg")
@ViewScoped
public class BitReclasificarSaldoNeg implements Serializable {

  private static final Logger log = LogManager.getLogger(BitReclasificarSaldoNeg.class);
  private static final String filePath = "IMX/exportar/";
  private final GenericService genericService = ServiceLocator.getGenericService();
  private String nombreArchivo = "BitacoraRecSaldoNegativo";
  private List<FiBitacoraImx> listaBitacoraReclasificacion;
  private /*DefaultSelectionItems*/ List<SelectItem> listaCalendar = new ArrayList<>(); /*DefaultSelectionItems();*/
  private /*DefaultSelectionItems*/ List<SelectItem> listaCampoA = new ArrayList<>(); /*DefaultSelectionItems();*/
  private UsuarioDTO usuarioLogueado;
  private String filtroCalendar;
  private String filtroCampoA;
  private boolean paginatorVisible;
  private int defaultRows = Constantes.REGISTROS;

  public BitReclasificarSaldoNeg() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("BitacoraReclasificarSaldoNegativo", usuarioLogueado.getRol());
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

  @SuppressWarnings("unchecked")
  private void init() {

    final String mes = ConsultasService.getMaxString("FI_BITACORA_IMX", "ATRIBUTO3");
    final String anio = ConsultasService.getMaxString("FI_BITACORA_IMX", "ATRIBUTO4");

    // CONSULTA PARA LLENAR EL COMBO DEL CAMPOA
    final List<VoLlaveValor> valoresCampoA = ConsultasService.getComboDistinct("FI_BITACORA_IMX",
        "CAMPOA", "NOM_CLIENTE",
        " TIPO_PROCESO = 'FIU' AND ATRIBUTO3 = '" + mes + "' AND ATRIBUTO4 = '" + anio +
            "' AND OPERACION_FIU = 'RCL' ",
        "order by 1");
    listaCampoA = Utilerias.creaSelectItem(valoresCampoA);

    // CONSULTA PARA LLENAR EL COMBO DEL CALENDARIO
    listaCalendar = Utilerias.creaSelectItem(Utilerias.getComboMesAnio());

    // CONSULTA PARA LA RECLASIFICACION DEL SALDO NEGATIVO
    listaBitacoraReclasificacion = genericService.get("FiBitacoraImx",
        " TIPO_PROCESO = 'FIU' AND ATRIBUTO3 = '" + mes + "' AND ATRIBUTO4 = '" + anio + "' " +
            " AND OPERACION_FIU = 'RCL' AND ATRIBUTO1 = 'S' ",
        " ORDER BY CUENTA_ORIGEN,CAMPOA,MONTO_CTA_CARGO DESC");

    for (final FiBitacoraImx bit : listaBitacoraReclasificacion) {
      bit.setMontoCtaOriFor(Utilerias.formatearNumero(bit.getMontoCtaOrigen(), ""));
      bit.setMontoCtaCarFor(Utilerias.formatearNumero(bit.getMontoCtaCargo(), ""));
      bit.setMontoCtaAboFor(Utilerias.formatearNumero(bit.getMontoCtaAbono(), ""));
    }

    paginatorVisible = (listaBitacoraReclasificacion.size() > 16);
    if (listaBitacoraReclasificacion.size() > 1) {
      exportarBitacoraReclasSaldoNegativo();
    }
  }

  @SuppressWarnings("unchecked")
  public void filtrarTableBitCancelacion() {
    if (filtroCalendar != null && !filtroCalendar.equals("-1")) {

      if (filtroCampoA.equals("-1")) {
        filtroCampoA = "";
      }

      String mes = filtroCalendar.substring(0, 2);
      String anio = filtroCalendar.substring(3, 7);

      setListaBitacoraReclasificacion(genericService.get("FiBitacoraImx", " TIPO_PROCESO = 'FIU' "
          + " AND CAMPOA = NVL('" + filtroCampoA + "',CAMPOA) AND ATRIBUTO3 = '" + mes + "' AND ATRIBUTO4 = '" + anio + "' AND OPERACION_FIU = 'RCL' AND ATRIBUTO1 = 'S'", " ORDER BY CUENTA_ORIGEN,CAMPOA,MONTO_CTA_CARGO DESC"));

      for (FiBitacoraImx fiBitacoraImx : listaBitacoraReclasificacion) {

        fiBitacoraImx.setMontoCtaOriFor(Utilerias.formatearNumero(fiBitacoraImx.getMontoCtaOrigen(), ""));
        fiBitacoraImx.setMontoCtaCarFor(Utilerias.formatearNumero(fiBitacoraImx.getMontoCtaCargo(), ""));
        fiBitacoraImx.setMontoCtaAboFor(Utilerias.formatearNumero(fiBitacoraImx.getMontoCtaAbono(), ""));
      }

    } else {
      mostrarMensaje("Debe seleccionar una fecha para realizar la busqueda.", false);
    }

    setPaginatorVisible(listaBitacoraReclasificacion.size() > 16);
    if (listaBitacoraReclasificacion.size() > 1) {
      try {
        exportarBitacoraReclasSaldoNegativo();
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    }
  }

  public void exportarBitacoraReclasSaldoNegativo() {
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
      sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));
      cell.setCellValue("BITACORA RECLASIFICAR SALDO NEGATIVO");
      cell.setCellStyle(style);

      final HSSFRow row = sheet.createRow((short) 1);
      row.createCell(0).setCellValue("CampoA");
      row.createCell(1).setCellValue("Nombre Cliente");
      row.createCell(2).setCellValue("Cuenta Origen");
      row.createCell(3).setCellValue("Monto");
      row.createCell(4).setCellValue("Cuenta Cargo");
      row.createCell(5).setCellValue("Monto");
      row.createCell(6).setCellValue("Cuenta Abono");
      row.createCell(7).setCellValue("Monto");

      final DecimalFormat decimalFormat = new DecimalFormat("###,###.00");

      int k = listaBitacoraReclasificacion.size() + 2;
      for (FiBitacoraImx bitacora : listaBitacoraReclasificacion) {
        k--;
        final HSSFRow row2 = sheet.createRow((short) k);

        row2.createCell(0).setCellValue(bitacora.getCampoA());
        row2.createCell(1).setCellValue(bitacora.getNomCliente());
        row2.createCell(2).setCellValue(bitacora.getCuentaOrigen());
        row2.createCell(3).setCellValue(bitacora.getMontoCtaOrigen() != null ?
            decimalFormat.format(bitacora.getMontoCtaOrigen()) : "0.0");
        row2.createCell(4).setCellValue(bitacora.getCuentaCargo());
        row2.createCell(5).setCellValue(bitacora.getMontoCtaCargo() != null ?
            decimalFormat.format(bitacora.getMontoCtaCargo()) : "0.0");
        row2.createCell(6).setCellValue(bitacora.getCuentaAbono());
        row2.createCell(7).setCellValue(bitacora.getMontoCtaAbono() != null ?
            decimalFormat.format(bitacora.getMontoCtaAbono()) : "0.0");
      }
      try (final FileOutputStream out = new FileOutputStream(archivo)) {
        wb.write(out);
      }
    } catch (Exception ex) {
      log.error("Error Creando Archivo Excel " + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("BitacoraReclasSaldoNegativo", "exportarBitacoraReclasSaldoNegativo", ex,
          "Error Creando Archivo Excel", usuarioLogueado);
    }
    log.info("Termina Exportacion Archivo Excel");
  }

}


