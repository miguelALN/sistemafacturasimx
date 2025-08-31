package com.bancomext.web.beans.admin;

import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiCatCuentasExtraccion;
import com.bancomext.service.mapping.FiCatCuentasExtraccionId;
import com.bancomext.web.utils.Constantes;
import com.bancomext.web.utils.Utilerias;
import com.bancomext.web.validator.ValidadorSesion;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.springframework.dao.DataIntegrityViolationException;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@ManagedBean(name = "configCuentasGeneracion")
@ViewScoped
public class ConfigCuentasGeneracion implements Serializable {

  private static final Logger log = LogManager.getLogger(ConfigCuentasGeneracion.class);
  private final GenericService genericService = ServiceLocator.getGenericService();
  private int defaultRows = Constantes.REGISTROS;
  private UsuarioDTO usuarioLogueado;
  private List<FiCatCuentasExtraccion> listaComparativo;
  private List<String> listaProducto;

  private FiCatCuentasExtraccion cuentaSeleccionada;
  private FiCatCuentasExtraccion cuentaNueva;
  private boolean paginatorVisible;


  public ConfigCuentasGeneracion() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("ConfigCuentasGeneracion", usuarioLogueado.getRol());
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
    listaComparativo = genericService.get("FiCatCuentasExtraccion", "", " Order by id.cta desc ");
    listaProducto = new ArrayList<>();
    for (final FiCatCuentasExtraccion cuenta : listaComparativo) {
      if (!listaProducto.contains(cuenta.getId().getProducto())) {
        listaProducto.add(cuenta.getId().getProducto());
      }
    }
    paginatorVisible = (getListaComparativo().size() > 16);
    cerrarPopupAlta();
  }

  public void abrirPopupAlta() {
    cuentaNueva = new FiCatCuentasExtraccion();
    cuentaNueva.setId(new FiCatCuentasExtraccionId());
  }

  private void cerrarPopupAlta() {
    PrimeFaces.current().executeScript("PF('altaDialog').hide()");
    PrimeFaces.current().ajax().update("form1:messages", "form1:dt-cuentas");
  }

  public void guardarCuenta() {
    log.info("Guardado Nueva Cuenta Factoraje IMX");

    try {
      if (camposValidos()) {
        genericService.save(cuentaNueva);
        mostrarMensaje("Registro creado correctamente", false);
        init();
        log.info("Registro creado correctamente");
      }
    } catch (DataIntegrityViolationException ex) {
      mostrarMensaje("La cuenta ya esta registrada", true);
      log.error(ex.getMessage(), ex);
    } catch (Exception ex) {
      mostrarMensaje("Error al intentar guardar", true);
      log.error("Error en Guardado registro " + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("ConfigCuentasGeneracion", "guardarPopup", ex,
          "Error en Guardado registro", usuarioLogueado);
    }
  }

  public void eliminarCuenta() {
    log.info("Eliminar Cuenta Factoraje IMX");
    try {
      if (genericService.get("FiCatCuentasExtraccion", "id.producto='" +
          cuentaSeleccionada.getId().getProducto() + "'", " Order by id.cta desc ").size() > 1) {
        genericService.delete(cuentaSeleccionada);
        mostrarMensaje("Registro eliminado correctamente", false);
        init();
        log.info("Cuenta eliminada correctamente");
      } else {
        mostrarMensaje("No se puede dejar un producto sin cuentas activas", true);
        log.info("No se puede dejar un producto sin cuentas activas");
      }
    } catch (DataIntegrityViolationException ex) {
      mostrarMensaje("No se puede eliminar la cuenta, primero debe eliminar las relaciones de éste con los módulos",
          true);
      log.error(ex.getMessage(), ex);
    } catch (Exception ex) {
      mostrarMensaje("Error al intentar eliminar", true);
      log.error("Error al Eliminar la cuenta Factoraje IMX " + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("ConfigCuentasGeneracion", "eliminar", ex,
          "Error al Eliminar la cuenta Factoraje IMX ", usuarioLogueado);
    }
  }

  public boolean camposValidos() {
    boolean validos = !Utilerias.esInvalida(cuentaNueva.getId().getCta());
    if (Utilerias.esInvalida(cuentaNueva.getId().getScta())) {
      validos = false;
    }
    if (Utilerias.esInvalida(cuentaNueva.getId().getSscta())) {
      validos = false;
    }
    if (Utilerias.esInvalida(cuentaNueva.getId().getSsscta())) {
      validos = false;
    }
    if (Utilerias.esInvalida(cuentaNueva.getId().getProducto())) {
      validos = false;
    }
    return validos;
  }

}

