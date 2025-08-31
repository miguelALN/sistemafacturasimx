package com.bancomext.web.beans.admin;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiAccesos;
import com.bancomext.service.mapping.FiBitacoraAccionesImx;
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@ManagedBean(name = "usuarios")
@ViewScoped
public class Usuarios implements Serializable {

  private static final Logger log = LogManager.getLogger(Usuarios.class);
  private final GenericService genericService = ServiceLocator.getGenericService();
  private final List<String> listaRoles = new ArrayList<>();
  private int defaultRows = Constantes.REGISTROS;
  private UsuarioDTO usuarioLogueado;
  private List<FiAccesos> listaAccesos;
  private FiAccesos usuarioSeleccionado;
  private FiAccesos usuarioNuevo = new FiAccesos();
  private boolean paginatorVisible;

  public Usuarios() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("Usuarios", usuarioLogueado.getRol());
    }
    log.info("GOINTO GET USERS COMBO");
    final List<VoLlaveValor> valores = ConsultasService.getComboDistinct("FI_CAT_ROLES", "ID_ROL",
        "DESCRIPCION", "", " ORDER BY DESCRIPCION");
    for (final VoLlaveValor v : valores) {
      listaRoles.add(v.getValor());
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
    log.info("INTO usuarios init");
    listaAccesos = genericService.get("FiAccesos", "", " order by fechaSalida asc ");
    actualizarBloqueo();
    paginatorVisible = (listaAccesos != null && listaAccesos.size() > 16);
    cerrarPopupAlta();
  }

  public void abrirPopupAlta() {
    usuarioNuevo = new FiAccesos();
  }

  private void cerrarPopupAlta() {
    PrimeFaces.current().executeScript("PF('altaDialog').hide()");
    PrimeFaces.current().ajax().update("form1:messages", "form1:dt-usuarios");
    usuarioNuevo = new FiAccesos();
  }

  @SuppressWarnings("unchecked")
  public void agregar() {
    log.info("Al agregar >>" + usuarioNuevo);
    if (usuarioNuevo.getIdPromotor() != null && !usuarioNuevo.getIdPromotor().isEmpty()) {
      try {
        genericService.save(usuarioNuevo);
        mostrarMensaje("Usuario " + usuarioNuevo.getIdPromotor() + " creado", false);
        listaAccesos = genericService.get("FiAccesos", "", " order by fechaSalida asc ");
        actualizarBloqueo();
        init();
      } catch (DataIntegrityViolationException ex) {
        mostrarMensaje("El usuario " + usuarioNuevo.getIdPromotor() + " ya existe", true);
        log.error(ex.getMessage(), ex);
      } catch (Exception e) {
        mostrarMensaje("Error al crear usuario " + usuarioNuevo.getIdPromotor(), true);
        log.error(e.getMessage(), e);
      }
    } else {
      mostrarMensaje("El Usuario es requerido.", true);
    }
  }

  @SuppressWarnings("unchecked")
  public void desbloquear() {
    usuarioSeleccionado.setFechaEntrada(new Date());
    usuarioSeleccionado.setFechaSalida(new Date());
    genericService.saveOrUpdate(usuarioSeleccionado);
    listaAccesos = genericService.get("FiAccesos", null, " order by fechaSalida asc ");
    paginatorVisible = (listaAccesos != null && listaAccesos.size() > 16);
    init();
    actualizarBloqueo();
  }

  public void eliminar() {
    log.info("Eliminar usuario factoraje IMX " + usuarioSeleccionado);
    try {
      if (!usuarioTieneAcciones()) {
        genericService.delete(usuarioSeleccionado);
        mostrarMensaje("Usuario " + usuarioSeleccionado.getIdPromotor() + " eliminado", false);
        init();
      } else {
        mostrarMensaje("No se puede eliminar, hay procesos parametrizados para " +
            usuarioSeleccionado.getIdPromotor(), true);
      }
    } catch (Exception ex) {
      log.error("Error en Eliminar Rol Factoraje IMX " + ex.getMessage(), ex);
      mostrarMensaje("NO se pudo eliminar al usuario " + usuarioSeleccionado.getIdPromotor(), false);
      Utilerias.guardarMensajeLog("CatRoles", "eliminarRol", ex,
          "Error en Eliminar Rol Factoraje IMX ", usuarioLogueado);
    }
  }

  private boolean usuarioTieneAcciones() {
    @SuppressWarnings("unchecked") final List<FiBitacoraAccionesImx> acciones =
        genericService.get("FiBitacoraAccionesImx", "", "");

    boolean tieneAcciones = false;
    for (final FiBitacoraAccionesImx listaBitacoraAccione : acciones) {
      if (listaBitacoraAccione.getAdicionadoPor().equals(usuarioSeleccionado.getIdPromotor())) {
        tieneAcciones = true;
        break;
      }
    }
    return tieneAcciones;
  }

  private void actualizarBloqueo() {
    for (final FiAccesos reg : listaAccesos) {
      reg.actualizaBloqueo();
    }
  }
}
