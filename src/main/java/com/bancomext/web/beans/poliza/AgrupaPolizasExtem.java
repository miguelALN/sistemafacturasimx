package com.bancomext.web.beans.poliza;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.ProcedimientosService;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.VoPolizasExtemporaneo;
import com.bancomext.service.mapping.VoPolizasExtemporaneoDetalle;
import com.bancomext.web.utils.Constantes;
import com.bancomext.web.utils.Utilerias;
import com.bancomext.web.validator.ValidadorSesion;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;

@Data
@ManagedBean(name = "agrupaPolizasExtem")
@ViewScoped
public class AgrupaPolizasExtem implements Serializable {

  private static final Logger log = LogManager.getLogger(AgrupaPolizasExtem.class);
  private List<VoPolizasExtemporaneo> listaPolizasExtemporaneas;
  private List<VoPolizasExtemporaneoDetalle> listaPolizasExtemDetalle;
  private String btnError;
  private String btnMensaje;
  private boolean ejecutaExtemporaneo = false;
  private boolean pagDetVisible = false;
  private boolean paginatorDetVisible;
  private boolean paginatorVisible;
  private int defaultRows = Constantes.REGISTROS;
  private UsuarioDTO usuarioLogueado;

  public AgrupaPolizasExtem() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("AgrupacionPolizasExtemporaneo", usuarioLogueado.getRol());
    }
    init();
  }

  private void init() {
    String mensaje = "";
    try {
      mensaje = ProcedimientosService.pAgrupaPolExtempor();
    } catch (Exception e) {
      btnError = mensaje;
      log.error(e.getMessage(), e);
    }
    if (mensaje != null) {
      btnError = mensaje;
    } else {
      listaPolizasExtemporaneas = ConsultasService.getPolizasExtemporaneo();
      for (VoPolizasExtemporaneo reg : listaPolizasExtemporaneas) {
        if (reg.getAbono() != null && reg.getCargo() != null) {
          reg.setAbonoFor(Utilerias.formatearNumero(reg.getAbono(), ""));
          reg.setCargoFor(Utilerias.formatearNumero(reg.getCargo(), ""));
        }
      }
      setPaginatorVisible(listaPolizasExtemporaneas != null && listaPolizasExtemporaneas.size() > 16);
    }
  }

  public void detalle() {
    String opCode = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("opCode");
    String descripcion = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("descripcion");
    String poliza = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("poliza");
    listaPolizasExtemDetalle = ConsultasService.getPolizasExtemporaneoDetalle(opCode, descripcion, Integer.parseInt(poliza));
    for (VoPolizasExtemporaneoDetalle reg : listaPolizasExtemDetalle) {
      if (reg.getAbono() != null && reg.getCargo() != null) {
        reg.setAbonoFor(Utilerias.formatearNumero(reg.getAbono(), ""));
        reg.setCargoFor(Utilerias.formatearNumero(reg.getCargo(), ""));
      }
    }
    setPaginatorDetVisible(listaPolizasExtemDetalle != null && listaPolizasExtemDetalle.size() > 16);
  }

  public void closePanel() {
  }

}
