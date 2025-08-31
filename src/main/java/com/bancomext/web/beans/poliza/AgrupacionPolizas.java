package com.bancomext.web.beans.poliza;

import com.bancomext.service.GenericService;
import com.bancomext.service.ProcedimientosService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiCierreAntesDespues;
import com.bancomext.web.utils.Constantes;
import com.bancomext.web.validator.ValidadorSesion;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@ManagedBean(name = "agrupacionPolizas")
@ViewScoped
public class AgrupacionPolizas implements Serializable {

  private static final Logger log = LogManager.getLogger(AgrupacionPolizas.class);
  private List<FiCierreAntesDespues> listaAgrupacionPolizas = new ArrayList<>();
  private UIComponent btnError;
  private UIComponent btnMensaje;
  private boolean paginatorVisible;
  private int defaultRows = Constantes.REGISTROS;
  private UsuarioDTO usuarioLogueado;

  public AgrupacionPolizas() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("AgrupacionPolizas", usuarioLogueado.getRol());
    }
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
  public void agrupaPolizas() {
    log.info("EJECUTA PROCESO DE AGRUPACION DE POLIZAS");
    String mensaje;
    try {
      mensaje = ProcedimientosService.comparaMontosPorMoneda();
      if (mensaje != null && !mensaje.isEmpty()) {
        mostrarMensaje(mensaje, false);
      } else {
        final GenericService genericService = ServiceLocator.getGenericService();
        setListaAgrupacionPolizas(genericService.get("FiCierreAntesDespues", "", " ORDER BY OPCODE, MONEDA, ATRB1"));
        mostrarMensaje("El proceso de Agrupacion de Polizas se mostro satisfactoriamente.", false);
        setPaginatorVisible(listaAgrupacionPolizas.size() > 16);
      }
    } catch (Exception e) {
      mostrarMensaje("Ha surgido un error al ejecutar el proceso" + e.getMessage(), true);
      log.error(e.getMessage(), e);
    }
  }
}
