package com.bancomext.web.beans.admin;

import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiCorreosPorReporte;
import com.bancomext.service.mapping.FiGposReporte;
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
import org.springframework.dao.DataIntegrityViolationException;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@ManagedBean(name = "catCorreoReportes")
@ViewScoped
public class CatCorreoReportes implements Serializable {

  private static final Logger log = LogManager.getLogger(CatCorreoReportes.class);
  private static final String filePath = "IMX/exportar/";
  private final GenericService genericService = ServiceLocator.getGenericService();
  private final int defaultRows = Constantes.REGISTROS;
  private UsuarioDTO usuarioLogueado;
  private List<FiGposReporte> grupos = new ArrayList<>();
  private List<FiCorreosPorReporte> listaFiCorreosPorReporte = new ArrayList<>();
  private FiCorreosPorReporte cuentaSeleccionada = new FiCorreosPorReporte();
  private FiCorreosPorReporte cuentaAnterior = new FiCorreosPorReporte();
  private String nombreArchivo = "CatalogoCorreosReporte";
  private boolean paginatorVisible;
  private boolean esModificacion;

  public CatCorreoReportes() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("CatCorreoReportes", usuarioLogueado.getRol());
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
    cerrarPopupEdicion();
    grupos = genericService.getAll("FiGposReporte");
    listaFiCorreosPorReporte = genericService.getAll("FiCorreosPorReporte");
    paginatorVisible = (listaFiCorreosPorReporte.size() > 16);
    if (!listaFiCorreosPorReporte.isEmpty()) {
      exportarCatalogoCorreosReporte();
    }
    PrimeFaces.current().ajax().update("form1:pgResultado");
  }

  public void abrirPopupAlta() {
    esModificacion = false;
    cuentaSeleccionada = new FiCorreosPorReporte();
  }

  public void abrirPopupEdicion() {
    esModificacion = true;
    cuentaAnterior = cuentaSeleccionada.clone();
  }

  private void cerrarPopupEdicion() {
    PrimeFaces.current().executeScript("PF('cuentasDialog').hide()");
    PrimeFaces.current().ajax().update("form1:messages", "form1:dt-cuentas");
  }

  public void guardarGpoCorreo() {
    try {
      if (camposValidos()) {
        if (esModificacion) {
          log.info("Modificando Correo Reportes");
          cuentaSeleccionada.setModificadoPor(usuarioLogueado.getUsuario());
          cuentaSeleccionada.setFechaModificacion(Calendar.getInstance().getTime());
          genericService.delete(cuentaAnterior);
          genericService.save(cuentaSeleccionada);
          mostrarMensaje("Cuenta modificada", false);
        } else {
          log.info("Guardando Correo Reportes");
          cuentaSeleccionada.setAdicionadoPor(usuarioLogueado.getUsuario());
          cuentaSeleccionada.setFechaAdicion(Calendar.getInstance().getTime());
          genericService.save(cuentaSeleccionada);
          mostrarMensaje("Cuenta dada de alta", false);
        }
        init();
      }
    } catch (DataIntegrityViolationException dex) {
      mostrarMensaje("El registro ya existe", true);
      log.error(dex.getMessage(), dex);
    } catch (Exception ex) {
      cerrarPopupEdicion();
      mostrarMensaje("Error al guardar", true);
      log.error("Error Guardando Correo Reportes", ex);
      Utilerias.guardarMensajeLog("CatCorreoReportes", "guardarGpoCorreo", ex,
          "Error Guardando Correo Reportes", usuarioLogueado);
    }
  }

  public void borrarGpoCorreo() {
    log.info("Borrando Correo Reportes");
    try {
      genericService.delete(cuentaSeleccionada);
      mostrarMensaje("Cuenta borrada", false);
      init();
    } catch (Exception ex) {
      mostrarMensaje("Error al borrar", true);
      log.error("Error Borrando Correo Reportes " + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("CatCorreoReportes", "borraGpoCorreo", ex,
          "Error Borrando Correo Reportes", usuarioLogueado);
    }
  }

  public boolean camposValidos() {
    boolean validos = true;
    final Pattern p =
        Pattern.compile("^(\\w+[.\\-]?)+@((\\[([0-9]{1,3}\\.){3}[0-9]{1,3}])|((\\w+[.\\-]?)+)([a-zA-Z]{2,4}))$");
    final Matcher m = p.matcher(cuentaSeleccionada.getId().getEmail());
    if (Utilerias.esInvalida(cuentaSeleccionada.getId().getEmail()) || !m.matches()) {
      validos = false;
    }
    return validos;
  }

  public void descargarArchivo() {
    LecturaArchivosHelper.descargarExcel(filePath, nombreArchivo);
  }

  public void exportarCatalogoCorreosReporte() {
    log.info("Inicia Exportacion Archivo Excel");

    nombreArchivo = UtileriasReportesExcel.borrarExportsExistentes(filePath, nombreArchivo);
    final File archivo = new File(filePath + nombreArchivo + ".xls");
    final boolean x = archivo.setWritable(true);
    log.debug(x);

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
      cell.setCellValue("CATALOGO DE CORREO POR REPORTE");
      cell.setCellStyle(style);

      final HSSFRow row = sheet.createRow((short) 1);
      row.createCell(0).setCellValue("Grupo Correo");
      row.createCell(1).setCellValue("Correo Electronio");

      int k = listaFiCorreosPorReporte.size() + 2;
      for (FiCorreosPorReporte reg : listaFiCorreosPorReporte) {
        k--;
        final HSSFRow row2 = sheet.createRow((short) k);
        row2.createCell(0).setCellValue(reg.getFiGposReporte().getDescripcion());
        row2.createCell(1).setCellValue(reg.getId().getEmail());
      }
      try (final FileOutputStream out = new FileOutputStream(archivo)) {
        wb.write(out);
      }
    } catch (Exception ex) {
      log.error("Error Creando Archivo Excel " + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("CatalogoCorreosReporte", "exportarCatalogoCorreosReporte", ex,
          "Error Creando Archivo Excel", usuarioLogueado);
    }
    log.info("Termina Exportacion Archivo Excel");
  }


}

