package com.bancomext.web.beans.primerdia;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiCofactoresImx;
import com.bancomext.service.mapping.FiFechasEjecucionImx;
import com.bancomext.web.utils.Constantes;
import com.bancomext.web.validator.ValidadorSesion;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Data
@ManagedBean(name = "primerDiaHabil")
@ViewScoped
public class PrimerDiaHabil implements Serializable {

  private static final Logger log = LogManager.getLogger(PrimerDiaHabil.class);
  private final GenericService genericService = ServiceLocator.getGenericService();
  private final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
  private Date fechaPrimerDia;
  private Date fechaUltimoDia;
  private String fechaPrimerDiaStr;
  private String fechaUltimoDiaStr;
  private FiFechasEjecucionImx beanFecha;
  private DataTable htmlDataTable;
  private Integer id;
  private List<FiFechasEjecucionImx> listaFechaEjecucion;
  private UsuarioDTO usuarioLogueado;
  private UIComponent btnError;
  private UIComponent btnMensaje;
  private UIComponent uImensajeGuardar;
  private UIComponent uImensajeModificar;
  private boolean popUp;
  private boolean saveEdit;
  private int defaultRows = Constantes.REGISTROS;
  private int rowIndex;

  public PrimerDiaHabil() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("PrimerDiaHabil", usuarioLogueado.getRol());
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
    setListaFechaEjecucion(genericService.get("FiFechasEjecucionImx", "", ""));
    dateFormat(listaFechaEjecucion);
  }

  @SuppressWarnings("unchecked")
  public void guardar() {

    FiFechasEjecucionImx bean = new FiFechasEjecucionImx();
    BigDecimal secuencia = ConsultasService.getMax("FI_FECHAS_EJECUCION_IMX", "ID");
    if (secuencia == null) {
      secuencia = new BigDecimal("0");
    }
    id = secuencia.intValue();
    id = id + 1;

    if (fechaPrimerDiaStr != null && fechaUltimoDiaStr != null) {

      log.info("fechaPrimerDiaStr " + fechaPrimerDiaStr  + " fechaUltimoDiaStr " + fechaUltimoDiaStr);

      String d = fechaPrimerDiaStr.substring(0, 2);
      String m = fechaPrimerDiaStr.substring(3, 5);
      String a = fechaPrimerDiaStr.substring(6, 10);

      String dh = fechaUltimoDiaStr.substring(0, 2);
      String mh = fechaUltimoDiaStr.substring(3, 5);
      String ah = fechaUltimoDiaStr.substring(6, 10);

      dh = String.valueOf(Integer.parseInt(dh) >= 31  ? 31 : Integer.parseInt(dh) + 1);

      String fechaUl = dh + "/" + mh + "/" + ah;
      DateFormat format = new SimpleDateFormat("dd/MM/yyyy");

      try {
        fechaPrimerDia = format.parse(fechaPrimerDiaStr);
        fechaUltimoDia = format.parse(fechaUltimoDiaStr);

      } catch (ParseException e) {
        log.error(e.getMessage(), e);
      }

      bean.setId(id);
      java.sql.Date sqlDate = new java.sql.Date(fechaPrimerDia.getTime());
      bean.setFecPrimerDiaHabilMes(sqlDate);

      bean.setAnio(Integer.parseInt(a));
      bean.setMes(Integer.parseInt(m));
      bean.setDia(Integer.parseInt(d));

      java.sql.Date sqlDateUltimoDia = new java.sql.Date(fechaUltimoDia.getTime());
      bean.setFecUltimoDiaHabilMes(sqlDateUltimoDia);

      bean.setAnioH(Integer.parseInt(ah));
      bean.setMesH(Integer.parseInt(mh));
      bean.setDiaH(Integer.parseInt(dh));

      List<FiFechasEjecucionImx> listPrimerDia = genericService.get("FiFechasEjecucionImx", " ANIO = " + Integer.valueOf(a) + " AND MES = " + Integer.valueOf(m), " ORDER BY 1");
      List<FiFechasEjecucionImx> listUtilmoDia = genericService.get("FiFechasEjecucionImx", " ANIOH = " + Integer.valueOf(ah) + " AND MESH = " + Integer.valueOf(mh), " ORDER BY 1");

      if (!listPrimerDia.isEmpty() || !listUtilmoDia.isEmpty()) {
        mostrarMensaje("Solo puede insertar una fecha para el primer o ultimo dia habil. Fecha(s) localizadas : Primer Dia Habil  - > "
            + formatter.format(listPrimerDia.get(0).getFecPrimerDiaHabilMes()) + " Ultimo Dia Habil  -> "
            + formatter.format(listUtilmoDia.get(0).getFecUltimoDiaHabilMes()), true);
        closePanel();
      } else {
        try {
          genericService.save(bean);
          closePanel();
          setListaFechaEjecucion(genericService.get("FiFechasEjecucionImx", "", ""));
          dateFormat(listaFechaEjecucion);
          mostrarMensaje("El registro se guardo satisfactoriamente", false);
        } catch (Exception e) {
          log.error(e.getMessage(), e);
        }
      }
    } else {
      mostrarMensaje("Les fechas son requeridas.", true);
    }
  }

  @SuppressWarnings("unchecked")
  public void modificar() {
    FiFechasEjecucionImx bean = new FiFechasEjecucionImx();

    if (fechaPrimerDiaStr != null && fechaUltimoDiaStr != null) {

      String d = fechaPrimerDiaStr.substring(0, 2);
      String m = fechaPrimerDiaStr.substring(3, 5);
      String a = fechaPrimerDiaStr.substring(6, 10);

      String dh = fechaUltimoDiaStr.substring(0, 2);
      String mh = fechaUltimoDiaStr.substring(3, 5);
      String ah = fechaUltimoDiaStr.substring(6, 10);

      dh = String.valueOf(Integer.parseInt(dh) >= 31 ? 31 : Integer.parseInt(dh) + 1);

      String fechaUl = dh + "/" + mh + "/" + ah;
      DateFormat format = new SimpleDateFormat("dd/MM/yyyy");

      try {
        fechaPrimerDia = format.parse(fechaPrimerDiaStr);
        fechaUltimoDia = format.parse(fechaUltimoDiaStr);
      } catch (ParseException e) {
        log.error(e.getMessage(), e);
      }

      bean.setId(id);
      java.sql.Date sqlDate = new java.sql.Date(fechaPrimerDia.getTime());
      bean.setFecPrimerDiaHabilMes(sqlDate);

      bean.setAnio(Integer.parseInt(a));
      bean.setMes(Integer.parseInt(m));
      bean.setDia(Integer.parseInt(d));

      java.sql.Date sqlDateUltimoDia = new java.sql.Date(fechaUltimoDia.getTime());
      bean.setFecUltimoDiaHabilMes(sqlDateUltimoDia);

      bean.setAnioH(Integer.parseInt(ah));
      bean.setMesH(Integer.parseInt(mh));
      bean.setDiaH(Integer.parseInt(dh));

      try {
        genericService.update(bean);
        id = bean.getId();
        closePanel();
        setListaFechaEjecucion(genericService.get("FiFechasEjecucionImx", "", ""));
        dateFormat(listaFechaEjecucion);
        mostrarMensaje("El registro se modifico satisfactoriamente", false);
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    } else {
      mostrarMensaje("Les fechas son requeridas.", true);
    }
  }

  @SuppressWarnings("unchecked")
  public void eliminar() {

    FiFechasEjecucionImx bean;
    try {
      log.info("Eliminar Ajuste id " + getId());

      Predicate<FiFechasEjecucionImx> primerDiaHabilImxPredicate = diaHabil -> diaHabil.getId().equals(getId());
      Optional<FiFechasEjecucionImx> optionalPrimerDiaHabilImx =  getListaFechaEjecucion().stream().filter(primerDiaHabilImxPredicate).findFirst();
      if (optionalPrimerDiaHabilImx.isPresent()) {
        bean = optionalPrimerDiaHabilImx.get();
        genericService.delete(bean);
        setListaFechaEjecucion(genericService.get("FiFechasEjecucionImx", "", ""));
        dateFormat(listaFechaEjecucion);
      }
      mostrarMensaje("Registro eliminado satisfactorimente", false);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
  }

  public void muestraEdicion() {

    log.info("into muestraEdicion ID " + id);

    setPopUp(true);
    setSaveEdit(false);

    FiFechasEjecucionImx bean;

    Predicate<FiFechasEjecucionImx> predicateFechaPrimerDia = fechaPrimerdia -> fechaPrimerdia.getId().equals(id);
    Optional<FiFechasEjecucionImx> optionalFiFechasEjecucionImx = getListaFechaEjecucion().stream().filter(predicateFechaPrimerDia).findFirst();; //.get(rowIndex);

    if (optionalFiFechasEjecucionImx.isPresent()) {
      bean = optionalFiFechasEjecucionImx.get();
      id = bean.getId();
      fechaPrimerDia = bean.getFecPrimerDiaHabilMes();
      fechaUltimoDia = bean.getFecUltimoDiaHabilMes();

      fechaPrimerDiaStr = formatter.format(fechaPrimerDia);
      fechaUltimoDiaStr = formatter.format(fechaUltimoDia);

      log.info("fechaPrimerDia " + fechaPrimerDia + "--" + fechaPrimerDiaStr);
      log.info("fechaUltimoDia " + fechaUltimoDia + "--" + fechaUltimoDiaStr);
    }

  }

  public void dateFormat(List<FiFechasEjecucionImx> lista) {
    if (lista != null && !lista.isEmpty()) {
      for (FiFechasEjecucionImx fiFechasEjecucionImx : lista) {
        fiFechasEjecucionImx.setFechaPrimerDia(formatter.format(fiFechasEjecucionImx.getFecPrimerDiaHabilMes()));
        fiFechasEjecucionImx.setFechaUltimoDia(formatter.format(fiFechasEjecucionImx.getFecUltimoDiaHabilMes()));
      }
    }
  }

  public void clear() {
    setId(0);
    setFechaPrimerDia(null);
    setFechaUltimoDia(null);
  }

  public void muestraInsert() {
    setSaveEdit(true);
    setPopUp(true);
    id = 0;
    fechaPrimerDiaStr = "";
    fechaUltimoDiaStr = "";
  }

  public void closePanel() {
    setPopUp(false);
    clear();
  }

  public void checkFechas() {
    log.info("INTO checkUncheckOption msg " + fechaPrimerDiaStr + " -- " + fechaUltimoDiaStr);
  }


}
