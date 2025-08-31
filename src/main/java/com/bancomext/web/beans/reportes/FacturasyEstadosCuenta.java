package com.bancomext.web.beans.reportes;

import com.bancomext.service.GenericService;
import com.bancomext.service.ProcedimientosService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiCfdHistoricoFolios;
import com.bancomext.service.mapping.FiCfdVerificacion;
import com.bancomext.web.utils.Constantes;
import com.bancomext.web.utils.UtileriasReportesExcel;
import com.bancomext.web.validator.ValidadorSesion;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.primefaces.component.datatable.DataTable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
@ManagedBean(name = "facturasyEstadosCuenta")
@ViewScoped
public class FacturasyEstadosCuenta implements Serializable {

  private static final Logger log = LogManager.getLogger(FacturasyEstadosCuenta.class);
  private static final String filePath = "IMX/exportar/";
  private final GenericService genericService = ServiceLocator.getGenericService();
  private String nombreArchivo = "Folio_SAT";
  private Date fechaFinal;
  private Date fechaGeneracion;
  private Date fechaInicial;
  private DataTable htmlDataTableCfdHistoricoFolios;
  private List<FiCfdHistoricoFolios> listaTablaCfdHistoricoFolios;
  private UsuarioDTO usuarioLogueado;
  private String nombreArchivoFacturas;
  private UIComponent btnFiltrar;
  private boolean paginatorVisibleCfdHistoricoFolios;
  private int defaultRows = Constantes.REGISTROS;

  public FacturasyEstadosCuenta() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("FacturasyEdosdeCta", usuarioLogueado.getRol());
    }
  }

  @SuppressWarnings("unchecked")
  public void filtrar() {
    DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
    Calendar cal1 = Calendar.getInstance();
    Calendar cal2 = Calendar.getInstance();
    Calendar cal3 = Calendar.getInstance();

    try {

      if ((fechaInicial != null && fechaFinal != null) || (fechaGeneracion != null)) {
        String f1 = "";
        String f2 = "";
        String f3 = "";

        if ((fechaGeneracion != null)) {
          cal3.setTime(fechaGeneracion);
          cal3.add(Calendar.DAY_OF_MONTH, 1);
          f3 = fechaGeneracion != null ? formato.format(fechaGeneracion) : "";
        }
        if ((fechaInicial != null && fechaFinal != null)) {
          cal1.setTime(fechaInicial);
          cal1.add(Calendar.DAY_OF_MONTH, 1);
          //fechaInicial = cal1.getTime();
          cal2.setTime(fechaFinal);
          cal2.add(Calendar.DAY_OF_MONTH, 1);
          //fechaFinal = cal2.getTime();
          f1 = fechaInicial != null ? formato.format(fechaInicial) : "";
          f2 = fechaFinal != null ? formato.format(fechaFinal) : "";
        }

        StringBuilder condicion = new StringBuilder();
        condicion.append(" (statusFolio != 'DISPONIBLE' OR statusFolio IS NULL) " + "@");

        condicion.append(f1.compareTo("") != 0 && f2.compareTo("") != 0 ?
            " TRUNC(fechaAsignacion) BETWEEN to_date('" + f1 + "', 'dd/mm/yyyy' ) " +
                "AND to_date( '" + f2 + "', 'dd/mm/yyyy' )@" : "");

        condicion.append(f3.compareTo("") != 0 ?
            " TRUNC(fechaAdicion) = to_date('" + f3 + "', 'dd/mm/yyyy' ) @" : "");

        String[] con = condicion.toString().split("@");
        condicion = new StringBuilder();

        for (int i = 0; i < con.length; i++) {
          condicion.append(i == 0 ? con[i] : " and" + con[i]);
        }


        if ((fechaInicial != null && fechaFinal != null)) {
          cal1.add(Calendar.DAY_OF_MONTH, -1);
          //fechaInicial = cal1.getTime();
          cal2.add(Calendar.DAY_OF_MONTH, -1);
          //fechaFinal = cal2.getTime();
        }

        setListaTablaCfdHistoricoFolios(genericService.get("FiCfdHistoricoFolios", condicion.toString(), "  order by id.numeroFolio asc "));

        for (FiCfdHistoricoFolios historico : getListaTablaCfdHistoricoFolios()) {

          List<FiCfdVerificacion> verificacion = genericService.get("FiCfdVerificacion"
              , " TRUNC(id.fechaEmision)= to_date('" + df.format(historico.getFechaAsignacion()) + "','dd/mm/yyyy') " +
                  " AND UPPER(status)='VERIFICADO'" +
                  " AND id.noAcreditado =" + historico.getCodigoCliente() +
                  " AND id.contrato ='" + historico.getContrato() + "' "
              , " order by id.fechaEmision desc");
          Calendar cal = Calendar.getInstance();

          if (!verificacion.isEmpty())
            cal.setTime(verificacion.get(0).getFecha());
          else
            cal.setTime(historico.getFechaModificacion());

          cal.add(Calendar.DATE, 3);
          final Date fecha = cal.getTime();


          final BigDecimal numeroFolio = historico.getId().getNumeroFolio();

          final String seEnvia;
          if (historico.getSerie() == null && numeroFolio == null) {
            seEnvia = "0";
          } else {
            seEnvia = "1";
          }

          final String serieFolioSat = ProcedimientosService.prFolioSatCredito(
              numeroFolio == null ? "" : numeroFolio.toPlainString(),
              historico.getProducto(),
              historico.getFechaAsignacion());
          historico.setSerieFolioSat(serieFolioSat);




          final String estatusGacd = ProcedimientosService.prEstatusCredito(
              numeroFolio == null ? "" : numeroFolio.toPlainString(),
              seEnvia,
              historico.getFechaAsignacion(),
              historico.getCodigoCliente().longValue(),
              historico.getStatusFolio(),
              historico.getProducto(),
              historico.getCodigoLineaCredito(),
              historico.getVersionInfor().longValue() != 0 ? historico.getVersionInfor().longValue() : 0
          );

          String D31052011 = "05/31/2011";
          String D01062011 = "06/01/2011";
          DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
          Date date31052011 = formatter.parse(D31052011);
          Date date01062011 = formatter.parse(D01062011);

          if (numeroFolio != null &&
              historico.getStatusFolio().equalsIgnoreCase("VERIFICADO") &&
              serieFolioSat == null &&
              (historico.getFechaAsignacion().before(new Date()) &&
                  historico.getFechaAsignacion().after(date31052011)))
            historico.setObservacionesFolio("CFD");
          else if (numeroFolio != null &&
              historico.getStatusFolio().equalsIgnoreCase("VERIFICADO") &&
              serieFolioSat != null &&
              estatusGacd.equalsIgnoreCase("EXITOSO") &&
              historico.getFechaAsignacion().before(date01062011))
            historico.setObservacionesFolio("CFDI VERIFICADO");
          else if (numeroFolio != null &&
              historico.getStatusFolio().equalsIgnoreCase("VERIFICADO") &&
              serieFolioSat != null &&
              estatusGacd.equalsIgnoreCase("CANCELADO") &&
              historico.getFechaAsignacion().before(date01062011))
            historico.setObservacionesFolio("CFDI CANCELADO EN GACD");
          else if (numeroFolio != null &&
              historico.getStatusFolio().equalsIgnoreCase("CONFIRMA_CANC") &&
              serieFolioSat != null &&
              estatusGacd.equalsIgnoreCase("CANCELADO") &&
              historico.getFechaAsignacion().before(date01062011))
            historico.setObservacionesFolio("CFDI POR CANCELAR ");
          else if (numeroFolio != null &&
              historico.getStatusFolio().equalsIgnoreCase("CANCELADO") &&
              serieFolioSat != null &&
              estatusGacd.equalsIgnoreCase("CANCELADO"))
            historico.setObservacionesFolio("CFDI CANCELADO");
          else if (numeroFolio != null &&
              historico.getStatusFolio().equalsIgnoreCase("VERIFICADO") &&
              serieFolioSat == null &&
              (!estatusGacd.equalsIgnoreCase("CANCELADO") &&
                  !estatusGacd.equalsIgnoreCase("EXITOSO") &&
                  !estatusGacd.equalsIgnoreCase("N") &&
                  !estatusGacd.equalsIgnoreCase("NO_DATA")) &&
              historico.getFechaAsignacion().before(date01062011))
            historico.setObservacionesFolio(estatusGacd);
          else if (numeroFolio != null &&
              historico.getStatusFolio().equalsIgnoreCase("VERIFICADO") &&
              serieFolioSat == null &&
              estatusGacd.equalsIgnoreCase("N") &&
              historico.getFechaAsignacion().before(date01062011) &&
              (new Date().after(fecha)))
            historico.setObservacionesFolio("CFDI EN TRAMITE SAT (72 HRS)");
          else if (numeroFolio != null &&
              historico.getStatusFolio().equalsIgnoreCase("VERIFICADO") &&
              serieFolioSat == null &&
              estatusGacd.equalsIgnoreCase("N") &&
              historico.getFechaAsignacion().before(date01062011) &&
              (new Date().before(fecha)))
            historico.setObservacionesFolio("REQUERIR RESPUESTA AL SAT");
          else if (numeroFolio != null &&
              historico.getStatusFolio().equalsIgnoreCase("VERIFICADO") &&
              serieFolioSat != null &&
              estatusGacd.equalsIgnoreCase("N") &&
              historico.getFechaAsignacion().before(date01062011))
            historico.setObservacionesFolio("SIN ESTATUS EN GACD");
          else if (numeroFolio != null &&
              historico.getStatusFolio().equalsIgnoreCase("VERIFICADO") &&
              serieFolioSat == null &&
              estatusGacd.equalsIgnoreCase("NO_DATA"))
            historico.setObservacionesFolio("NO EXISTE EN GACD");
          else if (numeroFolio == null &&
              historico.getStatusFolio() == null &&
              serieFolioSat == null &&
              estatusGacd.equalsIgnoreCase("EXITOSO"))
            historico.setObservacionesFolio("INFORMATIVO NOTIFICADO AL SAT");
          else if (numeroFolio == null &&
              historico.getStatusFolio() == null &&
              serieFolioSat == null &&
              estatusGacd.equalsIgnoreCase("CANCELADO"))
            historico.setObservacionesFolio("INFORMATIVO CANCELADO EN GACD");
          else if (numeroFolio == null &&
              Objects.requireNonNull(historico.getStatusFolio()).equalsIgnoreCase("CONFIRMA_CANC") &&
              serieFolioSat == null &&
              estatusGacd.equalsIgnoreCase("CANCELADO"))
            historico.setObservacionesFolio("INFORMATIVO POR CANCELAR");
          else if (numeroFolio == null &&
              historico.getStatusFolio().equalsIgnoreCase("CANCELADO") &&
              serieFolioSat == null &&
              estatusGacd.equalsIgnoreCase("CANCELADO"))
            historico.setObservacionesFolio("INFORMATIVO CANCELADO");
          else historico.setObservacionesFolio(" ");

          if (!verificacion.isEmpty())
            historico.setDescripcionCliente(verificacion.get(0).getFiFacturasEncabezado().getNombre());

          historico.setTipoCredito(verificacion.get(0).getFiFacturasEncabezado().getTipoCredito());


        }//fin for

        setPaginatorVisibleCfdHistoricoFolios(getListaTablaCfdHistoricoFolios() != null &&
            getListaTablaCfdHistoricoFolios().size() > 6);

        if (getListaTablaCfdHistoricoFolios().isEmpty())
          creaMensaje("La consulta no obtuvo resultados ", btnFiltrar);
        else
          exportarExcel();

      } else {
        String mensaje = fechaInicial == null && fechaFinal == null ? "La fecha inicial y Final" :
            fechaInicial == null ? "La fecha Inicial" : "la fecha final";
        creaMensaje("Debe ingresar " + mensaje, btnFiltrar);
      }
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
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
    nombreArchivo = UtileriasReportesExcel.borrarExportsExistentes(filePath, nombreArchivo);
    final File archivo = new File(filePath + nombreArchivo + ".xls");
    archivo.setWritable(true);

    try (final HSSFWorkbook wb = new HSSFWorkbook()) {
      final HSSFSheet sheet = wb.createSheet();
      final HSSFRow row = sheet.createRow((short) 0);

      row.createCell(0).setCellValue("Cartera");
      row.createCell(1).setCellValue("Serie");
      row.createCell(2).setCellValue("Fecha Asignacion");
      row.createCell(3).setCellValue("Cliente");
      row.createCell(4).setCellValue("Nombre");
      row.createCell(5).setCellValue("Linea de Credito");
      row.createCell(6).setCellValue("Moneda LC");
      row.createCell(7).setCellValue("Moneda Cuerpo");
      row.createCell(8).setCellValue("Folio SSUO");
      row.createCell(9).setCellValue("Folio SAT");
      row.createCell(10).setCellValue("Observaciones Folio");

      final DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
      int k = listaTablaCfdHistoricoFolios.size() + 1;
      for (FiCfdHistoricoFolios historico : getListaTablaCfdHistoricoFolios()) {
        k--;
        final HSSFRow row2 = sheet.createRow((short) k);
        row2.createCell(0).setCellType(Cell.CELL_TYPE_STRING);
        row2.createCell(1).setCellType(Cell.CELL_TYPE_STRING);
        row2.createCell(2).setCellType(Cell.CELL_TYPE_STRING);
        row2.createCell(3).setCellType(Cell.CELL_TYPE_STRING);
        row2.createCell(4).setCellType(Cell.CELL_TYPE_STRING);
        row2.createCell(5).setCellType(Cell.CELL_TYPE_STRING);
        row2.createCell(6).setCellType(Cell.CELL_TYPE_STRING);
        row2.createCell(7).setCellType(Cell.CELL_TYPE_STRING);
        row2.createCell(8).setCellType(Cell.CELL_TYPE_STRING);
        row2.createCell(9).setCellType(Cell.CELL_TYPE_STRING);
        row2.createCell(10).setCellType(Cell.CELL_TYPE_STRING);
        row2.createCell(0).setCellValue("1er Piso");
        row2.createCell(1).setCellValue("IMX");
        row2.createCell(2).setCellValue(
            historico.getFechaAsignacion() != null ? df.format(historico.getFechaAsignacion()) : "");
        row2.createCell(3).setCellValue(
            historico.getCodigoCliente().longValue() != 0 ? String.valueOf(historico.getCodigoCliente()) : "");
        row2.createCell(4).setCellValue(
            historico.getDescripcionCliente() != null ? historico.getDescripcionCliente() : "");
        row2.createCell(5).setCellValue(
            historico.getCodigoLineaCredito() != null ? historico.getCodigoLineaCredito() : "");
        row2.createCell(6).setCellValue(
            historico.getMoneda() != null ? historico.getMoneda() : "");
        row2.createCell(7).setCellValue(
            historico.getMonedaCuerpo() != null ? historico.getMonedaCuerpo() : "");
        row2.createCell(8).setCellValue(
            historico.getId().getNumeroFolio() != null ? historico.getId().getNumeroFolio().toString() : "");
        row2.createCell(9).setCellValue(
            historico.getSerieFolioSat() != null ? historico.getSerieFolioSat() : "");
        row2.createCell(10).setCellValue(
            historico.getObservacionesFolio() != null ? historico.getObservacionesFolio() : "");
      }
      try (final FileOutputStream out = new FileOutputStream(archivo)) {
        wb.write(out);
      }
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
  }

}


