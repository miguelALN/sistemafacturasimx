package com.bancomext.web.beans.admin;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiCatRoles;
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
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

@Data
@ManagedBean(name = "catRoles")
@ViewScoped
public class CatRoles implements Serializable {

  private static final Logger log = LogManager.getLogger(CatRoles.class);
  private final GenericService genericService = ServiceLocator.getGenericService();
  private final int defaultRows = Constantes.REGISTROS;
  private UsuarioDTO usuarioLogueado;
  private List<FiCatRoles> listaRoles;
  private FiCatRoles rolSeleccionado;
  private boolean paginatorVisible;

  public CatRoles() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("CatRoles", usuarioLogueado.getRol());
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
    listaRoles = genericService.get("FiCatRoles", "", " order by idRol asc");
    paginatorVisible = (listaRoles.size() > 16);
    cerrarPopupEdicion();
  }

  public void abrirPopupAlta() {
    rolSeleccionado = new FiCatRoles();
    final BigDecimal id = ConsultasService.getMax("FI_CAT_ROLES", "ID_ROL");
    rolSeleccionado.setIdRol(id.intValue() + 1);
  }

  private void cerrarPopupEdicion() {
    PrimeFaces.current().executeScript("PF('rolesDialog').hide()");
    PrimeFaces.current().ajax().update("form1:messages", "form1:dt-roles");
  }

  public void guardarRol() {
    log.info("Guardado Nuevo Rol Factoraje IMX");
    try {
      if (camposValidos()) {
        rolSeleccionado.setAdicionadoPor(usuarioLogueado.getUsuario());
        rolSeleccionado.setFechaAdicion(Calendar.getInstance().getTime());
        genericService.save(rolSeleccionado);
        mostrarMensaje("Rol " + rolSeleccionado.getDescripcion() + " creado correctamente", false);
        init();
        log.info("Rol " + rolSeleccionado.getDescripcion() + " creado correctamente");
      }
    } catch (DataIntegrityViolationException ex) {
      mostrarMensaje("El Id Rol ya esta registrado", true);
      log.error(ex.getMessage(), ex);
    } catch (Exception ex) {
      mostrarMensaje("Error al guardar", true);
      log.error("Error en Guardado Nuevo Rol Factoraje IMX id:" + rolSeleccionado.getIdRol() + " descripcion:" +
          rolSeleccionado.getDescripcion(), ex);
      Utilerias.guardarMensajeLog("CatRoles", "guardarRol", ex,
          "Error en Guardado Nuevo Rol Factoraje IMX id:" + rolSeleccionado.getIdRol() + " descripcion:" +
              rolSeleccionado.getDescripcion(), usuarioLogueado);
    }
  }

  public void eliminarRol() {
    log.info("Eliminar Rol Factoraje IMX");
    try {
      genericService.delete(getRolSeleccionado());
      mostrarMensaje("Rol " + rolSeleccionado.getDescripcion() + " eliminado correctamente", false);
      init();
      log.info("Rol " + rolSeleccionado.getDescripcion() + " eliminado correctamente");
    } catch (DataIntegrityViolationException ex) {
      mostrarMensaje("No se puede eliminar el rol, primero elimine las relaciones de éste con los módulos", true);
      log.error(ex.getMessage(), ex);
    } catch (Exception ex) {
      mostrarMensaje("Ocurrió un error al intentar Eliminar", true);
      log.error("Error en Eliminar Rol Factoraje IMX " + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("CatRoles", "eliminarRol", ex,
          "Error en Eliminar Rol Factoraje IMX", usuarioLogueado);
    }
  }

  private boolean camposValidos() {
    boolean validos = (rolSeleccionado.getIdRol() > 0);
    if (Utilerias.esInvalida(rolSeleccionado.getDescripcion())) {
      validos = false;
    }
    return validos;
  }
}
