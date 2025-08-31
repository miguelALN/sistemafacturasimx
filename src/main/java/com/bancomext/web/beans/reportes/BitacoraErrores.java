package com.bancomext.web.beans.reportes;

import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiCfdVerificacionBitaErr;
import com.bancomext.service.mapping.FiFacturasDetalle;
import com.bancomext.web.utils.Constantes;
import com.bancomext.web.utils.Utilerias;
import com.bancomext.web.utils.UtileriasReportesExcel;
import com.bancomext.web.validator.ValidadorSesion;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.primefaces.component.datatable.DataTable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Data
@ManagedBean(name = "bitacoraErrores")
@ViewScoped
public class BitacoraErrores implements Serializable {

  private static final Logger log = LogManager.getLogger(BitacoraErrores.class);
  private static final String filePath = "IMX/exportar/";
  private final GenericService genericService = ServiceLocator.getGenericService();
  private String nombreArchivo = "BitacoraErrores";
  private Date fechaFinal;
  private Date fechaGeneracion;
  private Date fechaInicial;
  private DataTable htmlDataTableCFDVerificacion;
  private List<FiCfdVerificacionBitaErr> listaTablaCFDVerificacion;
  private UsuarioDTO usuarioLogueado;
  private String nombreArchivoFacturas;
  private UIComponent btnFiltrar;
  private boolean paginatorVisibleCFDVerificacion;
  private int defaultRows = Constantes.REGISTROS;

  public BitacoraErrores() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("BitacoraErrores", usuarioLogueado.getRol());
    }
    init();
  }

  @SuppressWarnings("unchecked")
  private void init() {
    listaTablaCFDVerificacion =
        genericService.get("FiCfdVerificacionBitaErr", " 1=1 ", "  order by idFolio asc ");
    exportarExcel();
  }

  public void creaMensaje(String error, UIComponent btn) {
    FacesMessage message = new FacesMessage();
    FacesContext context = FacesContext.getCurrentInstance();
    message.setDetail(error);
    message.setSummary(error);
    message.setSeverity(FacesMessage.SEVERITY_ERROR);
    context.addMessage(btn.getClientId(context), message);
  }


  public void exportarExcel() {

    log.info("Inicia Exportacion Archivo Excel");

    nombreArchivo = UtileriasReportesExcel.borrarExportsExistentes(filePath, nombreArchivo);
    final File archivo = new File(filePath + nombreArchivo + ".xls");
    archivo.setWritable(true);

    try (final HSSFWorkbook wb = new HSSFWorkbook()) {
      final HSSFFont font = wb.createFont();
      font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

      final HSSFSheet sheet = wb.createSheet();
      final HSSFRow row = sheet.createRow((short) 0);

      row.createCell(0).setCellValue("Folio");
      row.createCell(1).setCellValue("Estatus");
      row.createCell(2).setCellValue("Conciliacion");
      row.createCell(3).setCellValue("Fecha Valor");

      row.createCell(4).setCellValue("Producto");
      row.createCell(5).setCellValue("RFC");
      row.createCell(6).setCellValue("Moneda");
      row.createCell(7).setCellValue("Importe");

      row.createCell(8).setCellValue("Campo A");
      row.createCell(9).setCellValue("Campo B");

      row.createCell(10).setCellValue("Concepto");
      row.createCell(11).setCellValue("Importe Iva");
      row.createCell(12).setCellValue("Total Importe");
      row.createCell(13).setCellValue("Total Valorizado");
      row.createCell(14).setCellValue("Porcentaje Iva");

      row.createCell(15).setCellValue("Nombre");
      row.createCell(16).setCellValue("Pais");
      row.createCell(17).setCellValue("Estado");
      row.createCell(18).setCellValue("Municipio");
      row.createCell(19).setCellValue("Contrato");
      row.createCell(20).setCellValue("Fecha Generacion");

      final DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
      final DecimalFormat decimalFormat = new DecimalFormat("###,###.00");

      int k = listaTablaCFDVerificacion.size() + 1;

      for (FiCfdVerificacionBitaErr cotizacion : listaTablaCFDVerificacion) {
        k--;
        final HSSFRow row2 = sheet.createRow((short) k);
        row2.createCell(0).setCellValue(cotizacion.getIdFolio().toString());

        row2.createCell(1).setCellValue(cotizacion.getStatus());
        row2.createCell(2).setCellValue(cotizacion.getStatusConciliado());
        row2.createCell(3).setCellValue(df.format(cotizacion.getFiFacturasEncabezado().getId().getFechaValor()));

        row2.createCell(4).setCellValue(cotizacion.getFiFacturasEncabezado().getTipoCredito());
        row2.createCell(5).setCellValue(cotizacion.getFiFacturasEncabezado().getRfc());
        row2.createCell(6).setCellValue(cotizacion.getFiFacturasEncabezado().getCodigoMoneda());
        row2.createCell(7).setCellValue(cotizacion.getFiFacturasEncabezado().getImporte().toString());
        row2.createCell(8).setCellValue(cotizacion.getFiFacturasEncabezado().getId().getNumAcreditado());
        row2.createCell(9).setCellValue(cotizacion.getFiFacturasEncabezado().getCampoB() != null ?
            cotizacion.getFiFacturasEncabezado().getCampoB() : "");

        @SuppressWarnings("unchecked")
        List<FiFacturasDetalle> facDetalle = (List<FiFacturasDetalle>) genericService.get("FiFacturasDetalle",
            " id.secuencia =" + cotizacion.getFiFacturasEncabezado().getSecuencia(),
            " order by id.secuencia ");

        if (!facDetalle.isEmpty()) {
          row2.createCell(10).setCellValue(facDetalle.get(0).getConcepto());
          row2.createCell(11).setCellValue(decimalFormat.format(facDetalle.get(0).getImporteiva().doubleValue()));
          row2.createCell(12).setCellValue(decimalFormat.format(facDetalle.get(0).getTotalimporte().doubleValue()));
          row2.createCell(13).setCellValue(decimalFormat.format(facDetalle.get(0).getTotalvalorizado().doubleValue()));
          row2.createCell(14).setCellValue(facDetalle.get(0).getPorcIva().toString());
        } else {
          row2.createCell(10).setCellValue("");
          row2.createCell(11).setCellValue("");
          row2.createCell(12).setCellValue("");
          row2.createCell(13).setCellValue("");
          row2.createCell(14).setCellValue("");
        }
        row2.createCell(15).setCellValue(cotizacion.getFiFacturasEncabezado().getNombre());
        row2.createCell(16).setCellValue(cotizacion.getFiFacturasEncabezado().getPais());
        row2.createCell(17).setCellValue(cotizacion.getFiFacturasEncabezado().getEstado());
        row2.createCell(18).setCellValue(cotizacion.getFiFacturasEncabezado().getMunicipio());
        row2.createCell(19).setCellValue(cotizacion.getFiFacturasEncabezado().getId().getContrato());
        row2.createCell(20).setCellValue(df.format(cotizacion.getFecha()));
      }
      try (final FileOutputStream out = new FileOutputStream(archivo)) {
        wb.write(out);
      }
    } catch (Exception ex) {
      log.error("Error Creando Archivo Excel" + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("VerificaProcesosCFDI", "exportarExcelVerificacion", ex,
          "Error Creando Archivo Excel", usuarioLogueado);
    }
    log.info("Termina Exportacion Archivo Excel");
  }

}


