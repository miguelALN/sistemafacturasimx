package com.bancomext.web.beans.admin;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiParamRoles;
import com.bancomext.service.mapping.FiParamRolesId;
import com.bancomext.service.mapping.VoLlaveValor;
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
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@ManagedBean(name = "rolesModulos")
@ViewScoped
public class RolesModulos implements Serializable {

  private static final Logger log = LogManager.getLogger(RolesModulos.class);
  private final GenericService genericService = ServiceLocator.getGenericService();
  private final List<SelectItem> listaRoles;
  private FiParamRoles rolSeleccionado;
  private List<FiParamRoles> listaRolesModulos;
  private List<SelectItem> listaModulos;
  private UsuarioDTO usuarioLogueado;
  private short moduloAlta;
  private short rolAlta;
  private boolean paginatorVisible;
  private int defaultRows = Constantes.REGISTROS;

  public RolesModulos() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("RolesModulos", usuarioLogueado.getRol());
    }
    final List<VoLlaveValor> valorRoles = ConsultasService.getCombo("FI_CAT_ROLES", "ID_ROL",
        "DESCRIPCION", "", " order by DESCRIPCION asc");
    listaRoles = Utilerias.creaSelectItem(valorRoles);
    final List<VoLlaveValor> valorModulos = ConsultasService.getCombo("FI_MODULOS_MENU", "ID_MODULO",
        "DESCRIPCION", "", " order by DESCRIPCION asc");
    listaModulos = Utilerias.creaSelectItem(valorModulos);
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
    listaRolesModulos = genericService.get("FiParamRoles", "", " order by id.idRol asc ");
    paginatorVisible = (listaRolesModulos != null && listaRolesModulos.size() > 50);
    cerrarPopupAlta();
  }

  public void abrirPopupAlta() {
    moduloAlta = -1;
    rolAlta = -1;
  }

  private void cerrarPopupAlta() {
    PrimeFaces.current().executeScript("PF('altaDialog').hide()");
    PrimeFaces.current().ajax().update("form1:messages", "form1:dt-roles");
    moduloAlta = -1;
    rolAlta = -1;
  }

  public void guardar() {
    log.info("Guardado Nuevo Rol-Modulo Factoraje IMX");

    if (rolAlta == -1) {
      mostrarMensaje("Ingresar Rol", true);
      return;
    }

    if (moduloAlta == -1) {
      mostrarMensaje("Ingresar Módulo", true);
      return;
    }

    final FiParamRolesId rolId = new FiParamRolesId();
    rolId.setIdRol(rolAlta);
    rolId.setIdModulo(moduloAlta);
    final FiParamRoles rol = new FiParamRoles();
    rol.setId(rolId);
    rol.setAdicionadoPor(usuarioLogueado.getUsuario());
    rol.setFechaAdicion(new Date());

    try {
      genericService.saveOrUpdate(rol);
      mostrarMensaje("Registro guardado correctamente.", false);
      log.info("Registro guardado correctamente.");
      init();
    } catch (DataIntegrityViolationException ex) {
      mostrarMensaje("El ID ya esta registrado", true);
      log.error(ex.getMessage(), ex);
    } catch (Exception ex) {
      mostrarMensaje("Ocurrio un error al intentar Guardar", true);
      log.error("Error en Guardado Nuevo Rol-Modulo Factoraje IMX id:" + rolAlta + " modulo:" + getModuloAlta() + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("RolesModulos", "eliminar", ex,
          "Error en Guardado Nuevo Rol-Modulo Factoraje IMX id:" + rolAlta + " modulo:" + getModuloAlta(),
          usuarioLogueado);
    }
  }

  public void actualizarModulos() {
    log.info("Rol Alta=" + rolAlta);
    if (rolAlta != -1) {
      final List<VoLlaveValor> modulos = ConsultasService.getCombo("FI_MODULOS_MENU", "ID_MODULO",
          "DESCRIPCION",
          "ID_MODULO not in (select ID_MODULO from FI_PARAM_ROLES where ID_ROL=" + rolAlta + ")",
          " order by DESCRIPCION asc");
      listaModulos = Utilerias.creaSelectItem(modulos);
      log.info("Actualizamos listaModulos size=" + listaModulos.size());
      PrimeFaces.current().ajax().update("dialogs:modulo");
    }
  }

  public void eliminar() {
    log.info("Eliminar RolesModulos Factoraje IMX");

    try {
      genericService.delete(rolSeleccionado);
      mostrarMensaje("Registro eliminado correctamente", false);
      log.info("Registro eliminado correctamente");
      init();
    } catch (Exception ex) {
      mostrarMensaje("Ocurrio un error al Eliminar", true);
      log.error("Error en Eliminar RolesModulos Factoraje IMX " + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("RolesModulos", "eliminar", ex,
          "Error en Eliminar RolesModulos Factoraje IMX", usuarioLogueado);
    }
  }
}
