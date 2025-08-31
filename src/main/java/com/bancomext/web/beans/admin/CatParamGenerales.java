package com.bancomext.web.beans.admin;

import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiCuerpoCorreos;
import com.bancomext.service.mapping.FiParamGenerales;
import com.bancomext.web.utils.Utilerias;
import com.bancomext.web.validator.ValidadorSesion;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

@Data
@ManagedBean(name = "catParamGenerales")
@ViewScoped
public class CatParamGenerales implements Serializable {

  private static final Logger log = LogManager.getLogger(CatParamGenerales.class);
  private final GenericService genericService = ServiceLocator.getGenericService();
  private UsuarioDTO usuarioLogueado;

  private FiParamGenerales parametroGeneral;
  private FiCuerpoCorreos cuerpoCorreos;
  private List<FiCuerpoCorreos> listaCuerpoCorreos;
  private boolean editables = false;

  public CatParamGenerales() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("CatParamGenerales", usuarioLogueado.getRol());
    }
    init();
  }

  @SuppressWarnings("unchecked")
  private void init() {
    final List<FiParamGenerales> listaParamGrales = genericService.getAll("FiParamGenerales");
    if (listaParamGrales != null && !listaParamGrales.isEmpty()) {
      parametroGeneral = listaParamGrales.get(0).clone();
    }
    listaCuerpoCorreos = genericService.getAll("FiCuerpoCorreos");
    cuerpoCorreos = new FiCuerpoCorreos();
    editables = false;
    PrimeFaces.current().ajax().update("form1:paramGrales");
  }

  public void switchEdicion() {
    editables = !editables;
  }

  public void actualizarCampos() {
    if ("0".equals(cuerpoCorreos.getCorreo())) {
      cuerpoCorreos.setAsunto(null);
      cuerpoCorreos.setCuerpo(null);
    } else {
      for (final FiCuerpoCorreos c : listaCuerpoCorreos) {
        if (c.getCorreo().equals(cuerpoCorreos.getCorreo())) {
          cuerpoCorreos.setAsunto(c.getAsunto());
          cuerpoCorreos.setCuerpo(c.getCuerpo());
          break;
        }
      }
    }
    PrimeFaces.current().ajax().update("form1:paramGrales");
  }

  @SuppressWarnings("unchecked")
  public void guardar() {
    try {
      if (diasValidos()) {
        parametroGeneral.setModificadoPor(usuarioLogueado.getUsuario());
        parametroGeneral.setFechaModificacion(Calendar.getInstance().getTime());
        genericService.update(parametroGeneral);
      }

      // INICIA SAVE PARA CORREO
      if (camposValidos()) {
        final List<FiCuerpoCorreos> lst = genericService.get(
            "FiCuerpoCorreos", " CORREO = '" + cuerpoCorreos.getCorreo() + "'", "");
        if (lst.isEmpty()) {
          genericService.save(cuerpoCorreos);
        } else {
          genericService.update(cuerpoCorreos);
        }
      }
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Registros actualizados"));
      PrimeFaces.current().ajax().update("form1:messages");
      init();
    } catch (Exception e) {
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error al guardar"));
      PrimeFaces.current().ajax().update("form1:messages");
      log.error(e.getMessage(), e);
    }
  }

  private boolean diasValidos() {
    boolean validos = !Utilerias.esInvalida(parametroGeneral.getDiasReproceso());
    if (Utilerias.esInvalida(parametroGeneral.getDiasCifrasCtrl())) {
      validos = false;
    }
    return validos;
  }

  private boolean camposValidos() {
    boolean validos = !"0".equals(cuerpoCorreos.getCorreo());
    if (Utilerias.esInvalida(cuerpoCorreos.getAsunto())) {
      validos = false;
    }
    if (Utilerias.esInvalida(cuerpoCorreos.getCuerpo())) {
      validos = false;
    }
    return validos;
  }

  public void changeEvent() {
  }
}
