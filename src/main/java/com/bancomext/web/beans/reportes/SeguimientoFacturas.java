package com.bancomext.web.beans.reportes;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ProcedimientosService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiCfdHistoricoFolios;
import com.bancomext.service.mapping.VoSeguimientoEstatusFac;
import com.bancomext.web.helper.LecturaArchivosHelper;
import com.bancomext.web.utils.*;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Data
@ManagedBean(name = "seguimientoFacturas")
@ViewScoped
public class SeguimientoFacturas implements Serializable {

  private static final Logger log = LogManager.getLogger(SeguimientoFacturas.class);
  private static final String filePath = "IMX/exportar/";
  private final GenericService genericService = ServiceLocator.getGenericService();
  private final int defaultRows = Constantes.REGISTROS;
  private String nombreArchivo = "SeguimientoFacturas";
  private Date fechaInicial;
  private Date fechaFinal;
  private List<VoSeguimientoEstatusFac> listaCfds = new ArrayList<>();
  private List<VoSeguimientoEstatusFac> cfdsSeleccionados = new ArrayList<>();
  private String labelBotonVerificar;
  private String labelBotonCancelar;
  private String labelBotonEnviar;
  private String usuario;
  private String password;
  private String motivo;
  private boolean esCancelacion;
  private boolean paginadorVisible;
  private UsuarioDTO usuarioLogueado;
  private final SimpleDateFormat formatoddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");

  public SeguimientoFacturas() throws ParseException {

    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("FacturasyEdosdeCta", usuarioLogueado.getRol());
    }
    init();
    filtrarSegumientoFacturas();
  }

  private static void mostrarMensaje(final String msg, final boolean esError) {
    final FacesMessage mensaje = new FacesMessage();
    mensaje.setSummary(msg);
    mensaje.setDetail("");
    mensaje.setSeverity(esError ? FacesMessage.SEVERITY_ERROR : FacesMessage.SEVERITY_INFO);
    FacesContext.getCurrentInstance().addMessage(null, mensaje);
    PrimeFaces.current().ajax().update(":form1:messages");
  }

  private static void borrarArchivosEnIn() {
    log.info("Inicia clean de archivos en IN...");
    final File directorioIn = new File("IMX/IN/");
    final File[] files = directorioIn.listFiles();
    if (files != null) {
      long k = Arrays.stream(files).map(File::delete).count();
      log.debug(k + "archivos borrados");
    }
    log.info("Termina clean de archivos en IN...");
  }

  private void init() throws ParseException {

    labelBotonVerificar = "Reprocesar";
    labelBotonCancelar = "Cancelar";
    labelBotonEnviar = "Enviar";
    cfdsSeleccionados.clear();
    FacesContext facesContext = FacesContext.getCurrentInstance();
    Map<String, String> params = facesContext.getExternalContext().getRequestParameterMap();
    log.info("FECHA INICIAL " + params.get("fechaInicial"));

    formatoddMMyyyy.parse(params.get("fechaInicial"));
    formatoddMMyyyy.parse(params.get("fechaFinal"));

  }

  public void limpiarTabla() throws ParseException {
    log.info("limpiando tabla");
    init();
    listaCfds.clear();
    paginadorVisible = false;
  }

  // FILTRO DE BUSQUEDA PARA PANTALLA DE SEGUIMIENTO DE ESTATUS DE FACTURAS
  public void filtrarSegumientoFacturas() {
    log.info("INTO filtrarSegumientoFacturas tabla");
    if (fechaInicial == null) {
      mostrarMensaje("La fecha inicial es requerida", true);
      return;
    }
    if (fechaFinal == null) {
      mostrarMensaje("La fecha final es requerida", true);
      return;
    }
    try {
      listaCfds = ConsultasService.getStatusFacturas(fechaInicial, fechaFinal);
      paginadorVisible = (listaCfds.size() > 16);
      cfdsSeleccionados.clear();
      if (listaCfds.isEmpty()) {
        mostrarMensaje("No se encontró ningun registro para las fechas especificadas", false);
      } else {
        //log.info("no es empty " + listaCfds.size());
        //cfdsSeleccionados = listaCfds;
        //log.info("no es empty " + cfdsSeleccionados.size());
        exportarSeguimientoFacturas();
      }
    } catch (Exception e) {
      mostrarMensaje("Error al realizar la busqueda.", true);
      log.error(e.getMessage(), e);
    }
    PrimeFaces.current().ajax().update(":form1:dt-cfdis");
  }

  public void procesarSeleccion() {
    log.info("cfdsSeleccionados.size() =" + cfdsSeleccionados.size());
    final String num;
    if (hayCFDSseleccionados()) {
      num = (cfdsSeleccionados.size() > 1 ? " " + cfdsSeleccionados.size() + " CFDs seleccionados" :
          " 1 CFD seleccionado");
    } else {
      num = "";
    }
    labelBotonVerificar = "Reprocesar" + num;
    labelBotonCancelar = "Cancelar" + num;
    labelBotonEnviar = "Enviar" + num;
  }

  public boolean hayCFDSseleccionados() {
    return cfdsSeleccionados != null && !cfdsSeleccionados.isEmpty();
  }

  public void validarVerificacion() {
    esCancelacion = false;
    log.debug("Entramos a validaVerificacion...");
    final long porVerificar = cfdsSeleccionados.stream().filter(c -> c.getStatusVerificado().equals("CANCELADO")).count();
    log.info("porVerificar=" + porVerificar);
    if (porVerificar == cfdsSeleccionados.size()) {
      usuario = usuarioLogueado.getUsuario();
      log.info("usuario=" + usuario);
      PrimeFaces.current().ajax().update(":dialogs:confirma-usuario-content");
      PrimeFaces.current().executeScript("PF('usuarioDialog').show()");
    } else {
      mostrarMensaje("Uno o más registros seleccionados no han sido cancelados, revise el estatus y " +
          "vuelva a intentar", true);
    }
  }

  public void validarCancelacion() {
    log.info("INTO validarCancelacion");
    esCancelacion = true;

    log.info("BEFORE FILTER");

    final List<VoSeguimientoEstatusFac> filtro = cfdsSeleccionados.stream().filter(
              c -> c.getStatusVerificado().equals("VERIFICADO") || c.getStatusVerificado().equals("REPOSITORIO")).collect(Collectors.toList());

    if (!filtro.isEmpty()) {
      log.info("SELETION " + filtro.get(0).getStatus());
      usuario = usuarioLogueado.getUsuario();
      PrimeFaces.current().ajax().update(":dialogs:confirma-usuario-content");
      PrimeFaces.current().executeScript("PF('usuarioDialog').show()");
    } else {
      log.info("NO SELECTION ");
      mostrarMensaje("Uno o más registros seleccionados no han sido verificados, revise el estatus y " +
          "vuelva a intentar", true);
    }
  }

  public void cancelarCFD() {

    int contadorErrores = 0;
    log.info("INTO   cancelarCFD() cfdsSeleccionados " + cfdsSeleccionados.size());
    log.info("INTO   cancelarCFD() listaCfds " + listaCfds.size());

    for (final VoSeguimientoEstatusFac registro : cfdsSeleccionados) {

      try {
        log.info("INTO   cancelarCFD() " + registro.getSerieFolio());
        final String folio = registro.getSerieFolio().replaceAll("\\D", "");
        log.info("folio " + folio + " seleccionado "  +  registro.isSeleccionado());

        String error = "";

        //if (registro.isSeleccionado()) {
          error = ProcedimientosService.cancelaFolio(folio);
          if (error != null) {
            contadorErrores++;
            continue;
          }
          // ACTUALIZAMOS BITACORA DE ACCIONES
          BitacoraAcciones.actualizarAcciones("CANCELAR", folio, registro.getStatus());
        //}

      } catch (Exception e) {
        mostrarMensaje("Error al realizar la cancelación.\n" + e.getMessage(), true);
        log.error(e.getMessage());
      }
    }
    filtrarSegumientoFacturas();
    if (contadorErrores == 0) {
      mostrarMensaje("Cancelación correcta de todos los registros tests", false);
    } else {
      mostrarMensaje("No fue posible Cancelar " + contadorErrores + " registros", true);
    }
  }

  public void verificarCFD() {
    int contadorErrores = 0;
    for (final VoSeguimientoEstatusFac registro : cfdsSeleccionados) {
      try {
        //if (registro.isSeleccionado()) {
          final String folio = registro.getSerieFolio().replaceAll("\\D", "");
          @SuppressWarnings("unchecked") final List<FiCfdHistoricoFolios> listaHistorico =
                  genericService.get("FiCfdHistoricoFolios", " id.numeroFolio = " + folio, "order by 1");
          log.info("FOLIO : " + listaHistorico.get(0).getSecuencia().toPlainString());

          final String error = ProcedimientosService.pReprocesaInformacion(registro.getFechaValor(),
                  listaHistorico.get(0).getSecuencia().toPlainString());
          if (error != null) {
            contadorErrores++;
            continue;
          }
          // ACTUALIZAMOS BITACORA DE ACCIONES
          BitacoraAcciones.actualizarAcciones("REPROCESO", folio, registro.getStatus());
        //}
      } catch (Exception e) {
        mostrarMensaje("Error al Verificar.\n" + e.getMessage(), true);
        log.error(e.getMessage());
      }
    }
    filtrarSegumientoFacturas();
    if (contadorErrores == 0) {
      mostrarMensaje("Verificación correcta de todos los registros", false);
    } else {
      mostrarMensaje("No fue posible Verificar " + contadorErrores + " registros", true);
    }
  }

  public void comprobarUsuario() {

    log.info("password " + password);
    log.info("usuarioLogueado.getClave() " + usuarioLogueado.getClave());
    if (!password.equals(usuarioLogueado.getClave())) {
      mostrarMensaje("Password incorrecto", true);
    } else {
      PrimeFaces.current().executeScript("PF('usuarioDialog').hide()");
      if (esCancelacion) {
        PrimeFaces.current().executeScript("PF('alertaCancela').show()");
      } else {
        PrimeFaces.current().executeScript("PF('alertaVerifica').show()");
      }
    }
  }

  // INICIA EXTRACCION DE EXTRACCION DE SFTP
  public void enviarCFDI() {
    // Metodo que borra todos los archivos del directorio IN
    borrarArchivosEnIn();

    final List<String> listaFoliosSFTP = cfdsSeleccionados
        .stream()
        .map(VoSeguimientoEstatusFac::getSerieFolio)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    log.info("Folios a enviar: " + listaFoliosSFTP);
    UtileriaConexion.downloadRep(listaFoliosSFTP);

    // Comprimimos los archivos en la carpeta IN
    log.info("Comprimiendo Archivos...");
    GeneradorZip.generarArchivoZip();
    log.info("Termina Compresion de Archivos...");

    // SE LEEN TODOS LOS ARCHIVOS DE LA CARPETA ZIP
    log.info("Leyendo Carpeta ZIP...");
    final File[] archivosEnDirectorioZIP = new File("IMX/ZIP/").listFiles();

    if (archivosEnDirectorioZIP != null && archivosEnDirectorioZIP.length > 0) {

      int contador = 0;
      for (final File archivoZip : archivosEnDirectorioZIP) {

        final String nombreSinExtension =
            archivoZip.getName().replaceAll(".ZIP", "").replaceAll(".zip", "");
        String folio = "";
        String serie = "";
        boolean sinSerieFolio;
        try {
          folio = nombreSinExtension.split("_")[2].trim();
          serie = nombreSinExtension.split("_")[0].trim();
          sinSerieFolio = false;
        } catch (Exception ignored) {
          sinSerieFolio = true;
        }

        if (!sinSerieFolio && !folio.isEmpty() && !serie.isEmpty() && Utilerias.validaNumerico(folio)) {

          if (serie.equals("FACTORAJEIMX")) {
            if (EnvioCfdiCorreo.enviarFactoraje(folio, archivoZip)) {
              contador++;
            } else {
              mostrarMensaje("Error al enviar el folio " + folio, true);
            }
          }
        }
      }
      log.info("Email enviados a clientes FACTORAJE_IMX ------- >" + contador + "< ------");
      mostrarMensaje("Facturas enviadas con éxito", false);
      borrarArchivosEnIn();
    } else {
      mostrarMensaje("No se encontraron archivos a enviar. Por favor verifique.", true);
    }
    log.info("Proceso de envio terminado.");
    filtrarSegumientoFacturas();
  }

  public void descargarArchivo() {
    LecturaArchivosHelper.descargarExcel(filePath, nombreArchivo);
  }

  // METODO QUE LLENA El EXCEL DEL SEGUIMIENTO DE ESTATUS DE FACTURAS
  public void exportarSeguimientoFacturas() {
    log.info("Inicia exportarSeguimientoFacturas");

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
      sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 13));
      cell.setCellValue("Seguimiento Estatus Facturas");
      cell.setCellStyle(style);

      final HSSFRow row = sheet.createRow((short) 1);
      row.createCell(0).setCellValue("Fecha Valor");
      row.createCell(1).setCellValue("Codigo Cliente");
      row.createCell(2).setCellValue("Nombre Cliente");
      row.createCell(3).setCellValue("Contrato");
      row.createCell(4).setCellValue("Moneda");
      row.createCell(5).setCellValue("Producto");
      row.createCell(6).setCellValue("Folio");
      row.createCell(7).setCellValue("Total");
      row.createCell(8).setCellValue("Estatus Verificado");
      row.createCell(9).setCellValue("Fecha Verificacion");
      row.createCell(10).setCellValue("Fecha Ingreso GACD");
      row.createCell(11).setCellValue("Fecha SAT");
      row.createCell(12).setCellValue("Fecha Envio Cliente");
      row.createCell(13).setCellValue("Folio SAT");

      int k = listaCfds.size() + 2;
      for (VoSeguimientoEstatusFac seguimiento : listaCfds) {
        k--;
        final HSSFRow row2 = sheet.createRow((short) k);
        row2.createCell(0).setCellValue(
            seguimiento.getFechaValorFormat() != null ? seguimiento.getFechaValorFormat() : "");
        row2.createCell(1).setCellValue(seguimiento.getCodigoCliente());
        row2.createCell(2).setCellValue(
            seguimiento.getNombreCliente() != null ? seguimiento.getNombreCliente() : "");
        row2.createCell(3).setCellValue(
            seguimiento.getContrato() != null ? seguimiento.getContrato() : "");
        row2.createCell(4).setCellValue(
            seguimiento.getMoneda() != null ? seguimiento.getMoneda() : "");
        row2.createCell(5).setCellValue(
            seguimiento.getProducto() != null ? seguimiento.getProducto() : "");
        row2.createCell(6).setCellValue(
            seguimiento.getSerieFolio() != null ? seguimiento.getSerieFolio() : "");
        row2.createCell(7).setCellValue(
            seguimiento.getTotal() != null ? seguimiento.getTotal().doubleValue() : 0);
        row2.createCell(8).setCellValue(
            seguimiento.getStatusVerificado() != null ? seguimiento.getStatusVerificado() : "");
        row2.createCell(9).setCellValue(
            seguimiento.getFechaVerificacion() != null ? seguimiento.getFechaVerificacion() : "");
        row2.createCell(10).setCellValue(
            seguimiento.getFechaIngresoGacd() != null ? seguimiento.getFechaIngresoGacd() : "");
        row2.createCell(11).setCellValue(
            seguimiento.getFechaSatGacd() != null ? seguimiento.getFechaSatGacd() : "");
        row2.createCell(12).setCellValue(
            seguimiento.getFechaEnvioCliente() != null ? seguimiento.getFechaEnvioCliente() : "");
        row2.createCell(13).setCellValue(
            seguimiento.getFolioSAT() != null ? seguimiento.getFolioSAT() : "");
      }

      try (final FileOutputStream out = new FileOutputStream(archivo)) {
        wb.write(out);
      }
    } catch (Exception ex) {
      log.error("Error exportarSeguimientoFacturas " + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("SeguimientoFacturas", "exportarSeguimientoFacturas", ex,
          "Error exportarSeguimientoFacturas", usuarioLogueado);
    }
    log.info("Termina Exportacion Archivo Excel");
  }

}
