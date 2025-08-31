package com.bancomext.web.beans.proc;

import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiBitacoraAccionesImx;
import com.bancomext.web.beans.reportes.ReporteComparativo;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Data
@ManagedBean(name = "bitacoraAcciones")
@ViewScoped
public class BitacoraAcciones implements Serializable {

  private static final Logger log = LogManager.getLogger(BitacoraAcciones.class);
  private final GenericService genericService = ServiceLocator.getGenericService();
  private final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
  private Date fechaFinal;
  private Date fechaInicial;
  private List<FiBitacoraAccionesImx> listaBitacora;
  private String usuario;
  private UIComponent btnMensaje;
  private boolean paginatorVisible;
  private int defaultRows = Constantes.REGISTROS;
  private UsuarioDTO usuarioLogueado;

  public BitacoraAcciones() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("BitacoraDeAcciones", usuarioLogueado.getRol());
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
    listaBitacora = genericService.get(
        "FiBitacoraAccionesImx", "", " ORDER BY FECHA_ADICION DESC");
    setPaginatorVisible(listaBitacora != null && listaBitacora.size() > 16);
  }

  @SuppressWarnings("unchecked")
  public void filtroBitacora() {
    log.info("inicial " +  fechaInicial + " final " + fechaFinal + " usuario " + usuario);
    if (fechaInicial != null && fechaFinal != null) {
      listaBitacora = genericService.get("FiBitacoraAccionesImx", " ADICIONADO_POR LIKE('%" + usuario + "%')"
          + "	AND TRUNC(FECHA_ADICION) BETWEEN NVL(TO_DATE('" + formatter.format(fechaInicial) + "','dd/mm/yyyy'),TRUNC(FECHA_ADICION)) AND NVL(TO_DATE('" + formatter.format(fechaFinal) + "','dd/mm/yyyy'),TRUNC(FECHA_ADICION))", " ORDER BY FECHA_ADICION DESC");
      mostrarMensaje("Se realizo satisfactorimente la busqueda.", false);
      setPaginatorVisible(listaBitacora != null && listaBitacora.size() > 16);
    } else {
      mostrarMensaje("Las fechas son requeridas para realizar la busqueda.", true);
    }
  }

  public void updateCalendar() {
    log.info("INICIAL " + fechaInicial +  " FINAL " + fechaFinal + " usuario " + usuario);
  }

}
