package com.bancomext.web.beans.reportes;

import com.bancomext.dao.ConsultasDAO;
import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.VoCifrasTotalesEstatusFac;
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
@ManagedBean(name = "cifrasTotalesEstatusFactu")
@ViewScoped
public class CifrasTotalesEstatusFactu implements Serializable {

  private static final Logger log = LogManager.getLogger(CifrasTotalesEstatusFactu.class);

  private final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
  private final GenericService genericService = ServiceLocator.getGenericService();
  private Date filtroFechaFin;
  private Date filtroFechaIni;
  private String filtroFechaFinStr;
  private String filtroFechaIniStr;
  private List<VoCifrasTotalesEstatusFac> listaCifras;
  private UsuarioDTO usuarioLogueado;
  private UIComponent error;
  private UIComponent mensaje;
  private boolean paginatorVisible;
  private int defaultRows = Constantes.REGISTROS;

  public CifrasTotalesEstatusFactu() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("CifrasTotales", usuarioLogueado.getRol());
    }
    listaCifras =
        ConsultasService.getCifrasTotalesEstatusFac(new Date(), new Date());
  }

  private static void mostrarMensaje(final String msg, final boolean esError) {
    final FacesMessage mensaje = new FacesMessage();
    mensaje.setSummary(msg);
    mensaje.setDetail("");
    mensaje.setSeverity(esError ? FacesMessage.SEVERITY_ERROR : FacesMessage.SEVERITY_INFO);
    FacesContext.getCurrentInstance().addMessage(null, mensaje);
    PrimeFaces.current().ajax().update(":form1:messages");
  }

  // FILTRO DE BUSQUEDA PARA PANTALLA DE SEGUIMIENTO DE ESTATUS DE FACTURAS
  public void filtroCifras() {

    try {

      filtroFechaIni = formatter.parse(filtroFechaIniStr);
      filtroFechaFin = formatter.parse(filtroFechaFinStr);
      log.info("fecha inicio " + filtroFechaIni);
      log.info("fecha final " + filtroFechaFin);

      if (filtroFechaIni != null && filtroFechaFin != null) {
        log.info("fechaInicial " + filtroFechaIni + " fechaFinal " +  filtroFechaFin);
        listaCifras = ConsultasService.getCifrasTotalesEstatusFac(filtroFechaIni, filtroFechaFin);
        log.info("listaCifras " + listaCifras.size());
      } else {
        mostrarMensaje("Las fechas son requeridas", true);
      }

    } catch (Exception exception) {
      log.error("Erro al generar las cifras totales" + exception.getMessage());
    }

  }

  public void checkFechas() {
    log.info("INTO checkUncheckOption msg " + filtroFechaIni + " -- " + filtroFechaIni);
  }

}
