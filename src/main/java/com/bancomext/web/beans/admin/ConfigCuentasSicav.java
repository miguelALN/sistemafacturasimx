package com.bancomext.web.beans.admin;

import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiCatCuentasSicav;
import com.bancomext.service.mapping.FiCatCuentasSicavId;
import com.bancomext.web.utils.Constantes;
import com.bancomext.web.utils.Utilerias;
import com.bancomext.web.validator.ValidadorSesion;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.springframework.dao.DataIntegrityViolationException;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;

@Data
@ManagedBean(name = "configCuentasSicav")
@ViewScoped
public class ConfigCuentasSicav implements Serializable {

  private static final Logger log = LogManager.getLogger(ConfigCuentasSicav.class);
  private final GenericService genericService = ServiceLocator.getGenericService();
  private FiCatCuentasSicav bean;
  private DataTable htmlDataTable;
  private List<FiCatCuentasSicav> listaComparativo;
  private UsuarioDTO usuarioLogueado;
  private String cuenta;
  private String cuentaF;
  private String mensaje;
  private String nombreArchivoFacturas;
  private String subcuenta;
  private String subcuentaF;
  private String subsubcuenta;
  private String subsubcuentaF;
  private String subsubsubcuenta;
  private String subsubsubcuentaF;
  private UIComponent btnGuardar;
  private UIComponent confirmarEliminar;
  private UsuarioDTO usuario;
  private boolean paginatorVisible;
  private boolean ppRolVisible;
  private int defaultRows = Constantes.REGISTROS;
  private int placeholder;

  public ConfigCuentasSicav() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("ConfigCuentasSicav", usuarioLogueado.getRol());
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
    listaComparativo = genericService.get("FiCatCuentasSicav", " 1=1 ", " Order by id.cta desc ");
    paginatorVisible = (listaComparativo.size() > 6);
  }

  @SuppressWarnings("unchecked")
  public void consultar() {
    final String cond = (cuentaF != null && !cuentaF.isEmpty() ? " id.cta = '" + cuentaF + "' @" : "") +
        (subcuentaF != null && !subcuentaF.isEmpty() ? " id.scta = '" + subcuentaF + "' @" : "") +
        (subsubcuentaF != null && !subsubcuentaF.isEmpty() ? " id.sscta = '" + subsubcuentaF + "' @" : "") +
        (subsubsubcuentaF != null && !subsubsubcuentaF.isEmpty() ? " id.ssscta = '" + subsubsubcuentaF + "' @" : "");
    final String[] con = cond.split("@");
    final StringBuilder condicion = new StringBuilder();
    for (int i = 0; i < con.length; i++) {
      condicion.append(i == 0 ? con[i] : " and " + con[i]);
    }
    listaComparativo = genericService.get("FiCatCuentasSicav", condicion.toString(),
        " Order by id.cta desc ");
    paginatorVisible = (listaComparativo.size() > 6);
  }

  public void guardarPopup() {

    if (!validaPopup()) {
      bean = new FiCatCuentasSicav();

      try {
        log.info("Guardado Nuevo Rol Factoraje IMX");
        FiCatCuentasSicavId id = new FiCatCuentasSicavId();
        id.setCta(cuenta);
        id.setScta(subcuenta);
        id.setSscta(subsubcuenta);
        id.setSsscta(subsubsubcuenta);

        getBean().setId(id);
        genericService.save(getBean());

        init();

        mostrarMensaje("Registro creado correctamente", false);
        log.info("Registro creado correctamente");
        setBean(null);
        setCuenta("");
        setSubcuenta("");
        setSubsubcuenta("");
        setSubsubsubcuenta("");


        setPpRolVisible(false);
      } catch (DataIntegrityViolationException ex) {
        mostrarMensaje("La cuenta ya esta registrada", true);
        log.error(ex.getMessage(), ex);
      } catch (Exception ex) {
        mostrarMensaje("Ocurrio un error al intentar Guardar", true);
        log.error("Error en Guardado registro " + ex.getMessage(), ex);
        Utilerias.guardarMensajeLog("CatRoles", "guardarRol", ex,
            "Error en Guardado registro", usuarioLogueado);
      }
    } else {
      mostrarMensaje("Faltan campos obligatorios", true);
    }

  }

  public void eliminar() {
    int rowIndex;
    FiCatCuentasSicav registro;
    setBean(null);
    try {
      log.info("Eliminar Rol Factoraje IMX");
      rowIndex = Integer.parseInt(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("rowIndex"));
      registro = getListaComparativo().get(rowIndex);
      setBean(registro);
      genericService.delete(getBean());
      init();
      mostrarMensaje("Registro eliminado correctamente", false);
      log.info("Cuenta eliminada correctamente");
    } catch (DataIntegrityViolationException ex) {
      mostrarMensaje("No se puede eliminar el rol, debe eliminar primero las relaciones de este con los modulos ", true);
      log.error(ex.getMessage(), ex);
    } catch (Exception ex) {
      mostrarMensaje("Ocurrio un error al intentar Eliminar", true);
      log.error("Error en Eliminar Rol Factoraje IMX " + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("ConfigCuentasSicav", "eliminar", ex,
          "Error en Eliminar Rol Factoraje IMX ", usuarioLogueado);
    }
  }

  public boolean validaPopup() {
    boolean isError = false;
    String error;

    if (cuenta == null || cuenta.isEmpty()) {
      error = "campo obligatorio";
      isError = true;
      mostrarMensaje(error, true);
      setMensaje("Campo Obligatorio, Favor verificar.");
    }

    if (subcuenta == null || subcuenta.isEmpty()) {
      error = "campo obligatorio";
      isError = true;
      mostrarMensaje(error, true);
      setMensaje("Campo Obligatorio, Favor verificar.");
    }

    if (subsubcuenta == null || subsubcuenta.isEmpty()) {
      error = "campo obligatorio";
      isError = true;
      mostrarMensaje(error, true);
      setMensaje("Campo Obligatorio, Favor verificar.");
    }

    if (subsubsubcuenta == null || subsubsubcuenta.isEmpty()) {
      error = "campo obligatorio";
      isError = true;
      mostrarMensaje(error, true);
      setMensaje("Campo Obligatorio, Favor verificar.");
    }

    return isError;
  }

  public void agregar() {
    setPpRolVisible(true);
  }

  public void closePanel() {
    setPpRolVisible(false);
  }

  public void limpiar() {
    setCuenta(null);
    setSubcuenta(null);
    setSubsubcuenta(null);
    setSubsubsubcuenta(null);
    setCuentaF(null);
    setSubcuentaF(null);
    setSubsubcuentaF(null);
    setSubsubsubcuentaF(null);
    consultar();
  }
}

