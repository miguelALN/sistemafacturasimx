package com.bancomext.web.beans.oper;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ProcedimientosService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.*;
import com.bancomext.web.helper.LecturaArchivosHelper;
import com.bancomext.web.utils.Constantes;
import com.bancomext.web.utils.UtileriaCorreo;
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
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Data
@ManagedBean(name = "procFactoraje")
@ViewScoped
public class ProcFactoraje implements Serializable {

  private static final Logger log = LogManager.getLogger(ProcFactoraje.class);
  private static final String filePath = "IMX/exportar/";
  private final int defaultRows = Constantes.REGISTROS;
  private final UsuarioDTO usuarioLogueado;
  private Date fecha;
  private boolean tableVisible;
  private List<FiCfdCifrasCtrl> listaCifrasControl;
  private String nombreArchivo = "ProcesoFactoraje";
  private boolean paginatorVisible;
  private boolean botonProcesarVisible;

  public ProcFactoraje() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    log.info("usuarioLogueado" + usuarioLogueado.getUsuario() + usuarioLogueado.getRol());
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("ProcesoFactoraje", usuarioLogueado.getRol());
    }
    PrimeFaces.current().executeScript("PF('confirmarDialog').hide()");
    PrimeFaces.current().ajax().update("form1:messages", "form1:dt-cifras");
    listaCifrasControl = new ArrayList<>();
    tableVisible = false;
    botonProcesarVisible = false;
  }

  private static void enviarCorreo(final List<FiAccesos> listaCorreos, final String asunto, final String contenido) {
    if (listaCorreos != null && !listaCorreos.isEmpty()) {
      final InternetAddress[] to = new InternetAddress[listaCorreos.size()];
      for (int i = 0; i < listaCorreos.size(); i++) {
        log.info("Correo: " + listaCorreos.get(i).getEmail());
        try {
          if ("Desarrollo".equals(Constantes.AMBIENTE)) {
            to[i] = new InternetAddress(Constantes.MAIL_DESARROLLO);
          } else {
            to[i] = new InternetAddress(listaCorreos.get(i).getEmail());
          }
        } catch (AddressException ae) {
          log.error(ae.getMessage(), ae);
        }
      }
      UtileriaCorreo.enviarCorreo(to, null, asunto, contenido, "FACTORAJE", null);
    }
  }

  public void fechaSeleccionada() {
    log.info("Se cambia la fecha: " + fecha);
    if (fecha != null) {
      listaCifrasControl = new ArrayList<>();
      tableVisible = false;
      botonProcesarVisible = true;
      PrimeFaces.current().ajax().update("form1:messages", "form1:dt-cifras");
      PrimeFaces.current().ajax().update(":form1:messages", ":form1:pgInfo");
    }
  }

  public void procesar() {
    log.info("INTO procesar " + Constantes.PRODUCCION);
    if (Constantes.PRODUCCION) {
      procesar_PROD();
    } else {
      procesar_DEV();
    }
  }

  private void procesar_DEV() {
    if (fecha != null) {
      final SimpleDateFormat formatoddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");
      listaCifrasControl = ConsultasService.getListCifras(fecha, 0);
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage("Se procesaron las facturas de la fecha " + formatoddMMyyyy.format(fecha)));

      paginatorVisible = listaCifrasControl.size() > 16;
      tableVisible = !listaCifrasControl.isEmpty();
      botonProcesarVisible = false;
      fecha = null;
    } else {
      creaMensajeError("La fecha de proceso no debe estar vacía");
      botonProcesarVisible = false;
    }
    log.info("Tabla visible:" + tableVisible);
    PrimeFaces.current().ajax().update(":form1:messages", ":form1:pgInfo");
  }

  @SuppressWarnings("unchecked")
  private void procesar_PROD() {
    botonProcesarVisible = false;
    final SimpleDateFormat formatoddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");
    final GenericService genericService = ServiceLocator.getGenericService();
    final Calendar cal = Calendar.getInstance();

    try {

      /*String fechaProcesos = "01/10/2013";
      SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
      fecha = formato.parse(fechaProcesos);*/

      log.info("Proceso Manual de Factoraje IMX fecha:" + fecha);

      if (fecha != null) {
        cal.setTime(fecha);
        cal.roll(Calendar.DAY_OF_MONTH, (-1) *
            Integer.parseInt(ConsultasService.getStringParametro("DIAS_REPROCESO")));
        final String respuestaMain = ProcedimientosService.cfdiMain(fecha, "Generacion Manual.");
        log.info("Respuesta PKG_FI_FAC_CFDI_MAIN: " + respuestaMain);

        if (respuestaMain == null) {
          final String fechasProceso;
          boolean contadorVerificado = false;
          log.info("new date : " + formatoddMMyyyy.format(new Date()));

          final List<FiCfdVerificacion> listaTablaVerificacion =
              ConsultasService.getVerificacion(fecha, false, 0);

          if (listaTablaVerificacion.isEmpty()) {
            fechasProceso = "No hay facturas por processar con estatus GENERADO y CONCILIADOS";
            log.info(fechasProceso);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(fechasProceso));
          } else {

            for (FiCfdVerificacion registro : listaTablaVerificacion) {
              final String respuestaVerificacion = ProcedimientosService.puStatusFolios(
                  registro.getProducto(),
                  registro.getId().getFechaEmision(),
                  registro.getStatus(),       /*"VERIFICADO"*/
                  registro.getId().getNumAcreditado(),
                  registro.getId().getContrato(),
                  registro.getSecuencia());
              log.info("respuestaVerificacion:" + respuestaVerificacion);
              if (respuestaVerificacion != null) {
                log.info("Se otuvo respuesta diferente de nulo :producto::: " + registro.getProducto() +
                    " ::fecha emision::   " + registro.getId().getFechaEmision() +
                    " ::Estatus:   " + registro.getStatus() + ":::::" + registro.getId().getNumAcreditado() +
                    ":::contrato::" + registro.getId().getContrato() + "::secuencia::" + registro.getSecuencia());
              } else {
                try {
                  registro.setFolioIMX("IMX" + (
                      (List<FiCfdHistoricoFolios>) genericService.get("FiCfdHistoricoFolios",
                          " Trunc(fechaAsignacion) = to_date('" +
                              formatoddMMyyyy.format(registro.getId().getFechaEmision()) + "','dd/mm/yyyy')" +
                              " And codigoCliente =" + registro.getId().getNumAcreditado() +
                              " And contrato ='" + registro.getId().getContrato() + "'" +
                              " And statusFolio = 'VERIFICADO'",
                          "  order by id.numeroFolio asc ")).get(0).getId().getNumeroFolio()
                  );
                } catch (Exception ex) {
                  log.info("Error al asignar folio IMX de fatura Verificada fechaasignacion:" +
                      registro.getId().getFechaEmision() + " codigoCliente:" + registro.getId().getNumAcreditado() +
                      " contrato:" + registro.getId().getContrato());
                }
                contadorVerificado = true;
                registro.setStatus("VERIFICADO");
                registro.setModificadoPor(usuarioLogueado.getUsuario());
                registro.setFechaModificacion(Calendar.getInstance().getTime());
              }
            }
            log.info("Generando H1 hay al menos uno verificado: " + contadorVerificado);
            if (contadorVerificado && genericService.get("FiCfdEstadoCuenta",
                " trunc(fechaProceso)= to_date('" + formatoddMMyyyy.format(new Date()) + "','dd/mm/yyyy') " +
                    " and id.tramaId='T01H' ", "").isEmpty()) {
              ProcedimientosService.cfdPTrama1H(fecha, new Date());
            }
            fechasProceso = "Se procesaron las facturas de la fecha " + formatoddMMyyyy.format(fecha);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(fechasProceso));
          }
          log.info(fechasProceso);

          ProcedimientosService.organizaCifrasControl(new Date());
          listaCifrasControl = ConsultasService.getListCifras(fecha, 0);
          final List<FiSeguimientoImx> listaSeguimientoImx = ConsultasService.getSeguimientoIMX(fecha, fecha, "0");
          final String contenido = UtileriaCorreo.contenidoProcesoGeneracionFactoraje(
              usuarioLogueado.getUsuario(), fechasProceso, listaSeguimientoImx, listaCifrasControl);

          final String grupoAdministradores = ConsultasService.getGrupoAdministradores();

          /*enviarCorreo(genericService.get(
                  "FiAccesos", " upper(rol) = upper('" + grupoAdministradores + "')", ""),
              "Proceso Generacion de Facturas IMX " + formatoddMMyyyy.format(fecha), contenido);*/

          paginatorVisible = listaCifrasControl.size() > 16;
          tableVisible = !listaCifrasControl.isEmpty();
          botonProcesarVisible = false;
          fecha = null;
          PrimeFaces.current().ajax().update(":form1:messages", ":form1:pgInfo");
        } else {
          creaMensajeError("Ocurrio un error al procesar los registros.");
          log.info("Ocurrio un error al procesar los registros.");
        }
      } else {
        creaMensajeError("La fecha de proceso no debe estar vacía");
        botonProcesarVisible = false;
      }
    } catch (Exception ex) {
      creaMensajeError("Ocurrio un error al procesar");
      log.error("Error en Proceso Manual de Factoraje IMX fecha:" + fecha + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("ProcesoFactoraje", "procesar", ex,
          "Error en Proceso Manual de Factoraje IMX fecha:" + fecha, usuarioLogueado);
    }
  }

  private void creaMensajeError(final String msg) {
    final FacesMessage message = new FacesMessage();
    message.setDetail(msg);
    message.setSummary(msg);
    message.setSeverity(FacesMessage.SEVERITY_ERROR);
    FacesContext.getCurrentInstance().addMessage(null, message);
  }

  public void descargarArchivo() {
    exportarProcesoFactoraje();
    LecturaArchivosHelper.descargarExcel(filePath, nombreArchivo);
  }

  private void exportarProcesoFactoraje() {
    log.info("Inicia Exportacion Archivo Excel");

    nombreArchivo = UtileriasReportesExcel.borrarExportsExistentes(filePath, nombreArchivo);
    final File archivo = new File(filePath + nombreArchivo + ".xls");
    boolean r = archivo.setWritable(true);
    log.debug(r);

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
      cell.setCellValue("PROCESO MANUAL CFDI");
      cell.setCellStyle(style);

      final HSSFRow row = sheet.createRow((short) 1);
      row.createCell(0).setCellValue("Fecha Valor");
      row.createCell(1).setCellValue("Esperadas IMX");
      row.createCell(2).setCellValue("Verificadas");
      row.createCell(3).setCellValue("Enviadas GACD");
      row.createCell(4).setCellValue("Folio SAT");
      row.createCell(5).setCellValue("Enviadas Cliente");
      row.createCell(6).setCellValue("Canceladas");

      int k = listaCifrasControl.size() + 2;
      for (FiCfdCifrasCtrl reg : listaCifrasControl) {
        k--;
        final HSSFRow row2 = sheet.createRow((short) k);
        row2.createCell(0).setCellValue(reg.getFechaValor());
        row2.createCell(1).setCellValue(reg.getEsperadas());
        row2.createCell(2).setCellValue(reg.getVerificadas());
        row2.createCell(3).setCellValue(reg.getEnviadoCliente());
        row2.createCell(4).setCellValue(reg.getFolioSat());
        row2.createCell(5).setCellValue(reg.getEnviadoCliente());
        row2.createCell(6).setCellValue(reg.getCanceladas());
      }

      try (final FileOutputStream out = new FileOutputStream(archivo)) {
        wb.write(out);
      }
    } catch (IOException ex) {
      log.error("Error Creando Archivo Excel " + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("ProcesoFactoraje", "exportarProcesoFactoraje", ex,
          "Error Creando Archivo Excel", usuarioLogueado);
    }

    log.info("Termina Exportacion Archivo Excel");
  }


}