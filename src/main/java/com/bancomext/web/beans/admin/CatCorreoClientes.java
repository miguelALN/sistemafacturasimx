package com.bancomext.web.beans.admin;

import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiCorreosPorCliente;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@ManagedBean(name = "catCorreoClientes")
@ViewScoped
public class CatCorreoClientes implements Serializable {

  private static final Logger log = LogManager.getLogger(CatCorreoClientes.class);
  private static final String filePath = "IMX/exportar/";
  private final GenericService genericService = ServiceLocator.getGenericService();
  private final int defaultRows = Constantes.REGISTROS;
  private UsuarioDTO usuarioLogueado;
  private List<FiCorreosPorCliente> listaFiCorreosPorCliente;
  private FiCorreosPorCliente cuentaSeleccionada;
  private FiCorreosPorCliente fiGpoCorrreoAnt;
  private String nombreArchivo = "CatalogoCorreosCliente";
  private boolean paginatorVisible;
  private boolean esModificacion;

  public CatCorreoClientes() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("CatCorreoClientes", usuarioLogueado.getRol());
    }
    init();
  }

  @SuppressWarnings("unchecked")
  private void init() {
    listaFiCorreosPorCliente =
        genericService.get("FiCorreosPorCliente", "", " order by DESCRIPCION_CLIENTE");
    paginatorVisible = (listaFiCorreosPorCliente.size() > 16);
    cerrarPopupEdicion();
    if (!listaFiCorreosPorCliente.isEmpty()) {
      exportarCatalogoCorreosCliente();
    }
  }

  public void abrirPopupAlta() {
    esModificacion = false;
    cuentaSeleccionada = new FiCorreosPorCliente();
  }

  public void abrirPopupEdicion() {
    esModificacion = true;
    fiGpoCorrreoAnt = cuentaSeleccionada.clone();
  }

  private void cerrarPopupEdicion() {
    PrimeFaces.current().executeScript("PF('cuentasDialog').hide()");
    PrimeFaces.current().ajax().update("form1:messages", "form1:dt-cuentas");
  }

  public void guardarGpoCorreo() {
    try {
      if (camposValidos()) {
        if (esModificacion) {
          log.info("Modificando Correo Cliente");
          cuentaSeleccionada.setModificadoPor(usuarioLogueado.getUsuario());
          cuentaSeleccionada.setFechaModificacion(Calendar.getInstance().getTime());
          genericService.delete(fiGpoCorrreoAnt);
          genericService.save(cuentaSeleccionada);
          FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Cuenta modificada"));
        } else {
          log.info("Alta de Correo Cliente");
          cuentaSeleccionada.setAdicionadoPor(usuarioLogueado.getUsuario());
          cuentaSeleccionada.setFechaAdicion(Calendar.getInstance().getTime());
          genericService.save(cuentaSeleccionada);
          FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Cuenta dada de alta"));
        }
        init();
      }
    } catch (Exception ex) {
      cerrarPopupEdicion();
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error al guardar"));
      log.error("Error Guardando Correo Cliente " + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("CatCorreoClientes", "guardarGpoCorreo", ex,
          "Error Guardando Correo Cliente", usuarioLogueado);
    }
  }

  public void borrarGpoCorreo() {
    log.info("Borrando Correo Cliente");
    try {
      genericService.delete(cuentaSeleccionada);
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Cuenta borrada"));
      init();
    } catch (Exception ex) {
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error al borrar"));
      log.error("Error Borrando Correo Cliente " + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("CatCorreoClientes", "borraGpoCorreo", ex,
          "Error Borrando Correo Cliente", usuarioLogueado);
    }
  }

  private boolean camposValidos() {
    boolean validos = (cuentaSeleccionada.getId().getCodigoCliente() != 0);

    if (Utilerias.esInvalida(cuentaSeleccionada.getDescripcionCliente())) {
      validos = false;
    }

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

  public void exportarCatalogoCorreosCliente() {
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
      cell.setCellValue("CATALOGO DE CORREO POR CLIENTES");
      cell.setCellStyle(style);

      final HSSFRow row = sheet.createRow((short) 1);
      row.createCell(0).setCellValue("Codigo Cliente");
      row.createCell(1).setCellValue("Descripcion Cliente");
      row.createCell(2).setCellValue("E-Mail");

      int k = listaFiCorreosPorCliente.size() + 2;
      for (FiCorreosPorCliente reg : listaFiCorreosPorCliente) {
        k--;
        final HSSFRow row2 = sheet.createRow((short) k);
        row2.createCell(0).setCellValue(reg.getId().getCodigoCliente());
        row2.createCell(1).setCellValue(reg.getDescripcionCliente());
        row2.createCell(2).setCellValue(reg.getId().getEmail());
      }

      try (final FileOutputStream out = new FileOutputStream(archivo)) {
        wb.write(out);
      }
    } catch (IOException ex) {
      log.error("Error Creando Archivo Excel", ex);
      Utilerias.guardarMensajeLog("CatalogoCorreoClientes", "exportarCatalogoCorreosCliente", ex,
          "Error Creando Archivo Excel", usuarioLogueado);
    }

    log.info("Termina Exportacion Archivo Excel");
  }

}

