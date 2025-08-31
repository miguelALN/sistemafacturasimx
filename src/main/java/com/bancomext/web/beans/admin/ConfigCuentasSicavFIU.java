package com.bancomext.web.beans.admin;

import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiCatCuentasSicav;
import com.bancomext.service.mapping.FiParamGenerales;
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
import java.util.List;

@Data
@ManagedBean(name = "configCuentasSicavFIU")
@ViewScoped
public class ConfigCuentasSicavFIU implements Serializable {

  private static final Logger log = LogManager.getLogger(ConfigCuentasSicavFIU.class);
  private final GenericService genericService = ServiceLocator.getGenericService();
  private FiParamGenerales parametroGeneral;
  private FiCatCuentasSicav cuentaNueva;
  private FiCatCuentasSicav cuentaSeleccionada;
  private List<FiCatCuentasSicav> listaCuentasSicav;
  private UsuarioDTO usuarioLogueado;
  private boolean paginatorVisible;
  private int defaultRows = Constantes.REGISTROS;

  public ConfigCuentasSicavFIU() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("ConfigCuentasSicav", usuarioLogueado.getRol());
    }
    init();
  }

  private static boolean camposValidos(final FiCatCuentasSicav cuentaNueva) {
    if (cuentaNueva.getId().getCta() == null || cuentaNueva.getId().getCta().isEmpty()) {
      mostrarMensaje("La Cuenta es obligatoria", true);
      return false;
    }
    if (cuentaNueva.getId().getScta() == null || cuentaNueva.getId().getScta().isEmpty()) {
      mostrarMensaje("La Subcuenta es obligatoria", true);
      return false;
    }
    if (cuentaNueva.getId().getSscta() == null || cuentaNueva.getId().getSscta().isEmpty()) {
      mostrarMensaje("La Subsubcuenta es obligatoria", true);
      return false;
    }
    if (cuentaNueva.getId().getSsscta() == null || cuentaNueva.getId().getSsscta().isEmpty()) {
      mostrarMensaje("La Subsubsubcuenta es obligatoria", true);
      return false;
    }
    return true;
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
    final List<FiParamGenerales> listaParamGrales = genericService.getAll("FiParamGenerales");
    parametroGeneral = listaParamGrales.get(0).clone();
    listaCuentasSicav = genericService.get("FiCatCuentasSicav", "", " order by id.cta desc ");
    paginatorVisible = (getListaCuentasSicav().size() > 6);
    cuentaNueva = new FiCatCuentasSicav();
    cuentaSeleccionada = new FiCatCuentasSicav();
    cerrarPopupEdicion();
  }

  public void abrirPopupAlta() {
    cuentaNueva = new FiCatCuentasSicav();
  }

  private void cerrarPopupEdicion() {
    PrimeFaces.current().executeScript("PF('altaDialog').hide()");
    PrimeFaces.current().ajax().update("form1:messages", "form1:dt-cuentas");
  }

  public void guardar() {
    log.info("Guardado Cuenta Sicav Factoraje IMX");
    if (camposValidos(cuentaNueva)) {
      try {
        genericService.save(cuentaNueva);
        mostrarMensaje("Registro creado correctamente", false);
        init();
        log.info("Registro creado correctamente");
      } catch (DataIntegrityViolationException ex) {
        mostrarMensaje("La cuenta ya esta registrada", true);
        log.error(ex.getMessage(), ex);
      } catch (Exception ex) {
        mostrarMensaje("Error al intentar guardar", true);
        log.error("Error en Guardado registro " + ex.getMessage(), ex);
        Utilerias.guardarMensajeLog("ConfigCuentasSicavFIU", "guardar", ex,
            "Error en Guardado registro", usuarioLogueado);
      }
    } else {
      mostrarMensaje("Faltan campos obligatorios", true);
    }
  }

  public void eliminar() {
    log.info("Eliminar Cuenta Sicav Factoraje IMX");
    try {
      genericService.delete(cuentaSeleccionada);
      mostrarMensaje("Registro eliminado correctamente", false);
      init();
      log.info("Cuenta eliminada correctamente");
    } catch (DataIntegrityViolationException ex) {
      mostrarMensaje("No se puede eliminar la cuenta, debe eliminar primero las relaciones de éste",
          true);
      log.error(ex.getMessage(), ex);
    } catch (Exception ex) {
      mostrarMensaje("Error al intentar eliminar", true);
      log.error("Error al eliminar cuenta sicav FIU IMX " + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("ConfigCuentasSicavFIU", "eliminar", ex,
          "Error al eliminar cuenta sicav FIU IMX ", usuarioLogueado);
    }
  }

}

