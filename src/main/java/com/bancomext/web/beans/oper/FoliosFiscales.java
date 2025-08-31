package com.bancomext.web.beans.oper;

import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiCfdFolios;
import com.bancomext.service.mapping.FiCfdFoliosId;
import com.bancomext.service.mapping.FiCfdHistoricoFolios;
import com.bancomext.service.mapping.FiCfdHistoricoFoliosId;
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
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@ManagedBean(name = "foliosFiscales")
@ViewScoped
public class FoliosFiscales implements Serializable {

  private static final Logger log = LogManager.getLogger(FoliosFiscales.class);
  private final GenericService genericService = ServiceLocator.getGenericService();
  private BigDecimal foliosCancelados;
  private BigDecimal foliosDiponibles;
  private BigDecimal foliosUtilizados;
  private BigDecimal porcentajeFoliosDisponibles;
  private BigDecimal primerFolio;
  private BigDecimal ultimoFolio;
  private Date fechaAutorizacion;
  private FiCfdFolios tabla;
  private FiCfdFoliosId idTabla;
  private FiCfdHistoricoFolios tablaHistorico;
  private DataTable htmlDataTable;
  private Integer orden;
  private List<FiCfdFolios> listaTabla;
  private UsuarioDTO usuarioLogueado;
  private String noAutorizacion;
  private String producto;
  private String serie;
  private boolean paginatorVisible;
  private boolean popUpAlta;
  private int defaultRows = Constantes.REGISTROS;

  public FoliosFiscales() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("FoliosFiscales", usuarioLogueado.getRol());
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
    setListaTabla(genericService.get("FiCfdFolios", "", " order by id.fechaAutorizacion asc "));
    setPaginatorVisible(getListaTabla() != null && getListaTabla().size() > 16);
  }

  public void cancelarPopUpAlta() {
    setFechaAutorizacion(null);
    setNoAutorizacion(null);
    setPrimerFolio(null);
    setUltimoFolio(null);
    setFoliosUtilizados(null);
    setFoliosDiponibles(null);
    setPorcentajeFoliosDisponibles(null);
    setFoliosCancelados(null);
    setSerie(null);
    setPopUpAlta(false);
  }

  public void muestraPopUpAlta() {
    setPopUpAlta(true);
    setFechaAutorizacion(null);
    setNoAutorizacion(null);
    setPrimerFolio(null);
    setUltimoFolio(null);
    setFoliosUtilizados(null);
    setFoliosDiponibles(null);
    setPorcentajeFoliosDisponibles(null);
    setFoliosCancelados(null);
    setProducto("FACTORAJE INT");
    setSerie("IMX");
    setOrden(1);
    setTabla(new FiCfdFolios());
  }

  public boolean validaCamposAlta() {
    boolean sinerrores = true;
    if (getFechaAutorizacion() == null) {
      mostrarMensaje("Campo  requerido ", true);
    }
    if (getNoAutorizacion() == null || getNoAutorizacion().isEmpty()) {
      mostrarMensaje("Campo  requerido ", true);
    }
    if (getPrimerFolio() == null || getPrimerFolio().compareTo(new BigDecimal("0")) < 0) {
      mostrarMensaje("Campo  requerido y mayor a 0", true);
    }
    if (getUltimoFolio() == null || getUltimoFolio().compareTo(new BigDecimal("0")) < 0) {
      mostrarMensaje("Campo  requerido y mayor a 0", true);
    }
    if (getProducto() == null || getProducto().isEmpty()) {
      mostrarMensaje("Campo  requerido ", true);
    }
    if (getSerie() == null || getSerie().isEmpty()) {
      mostrarMensaje("Campo  requerido ", true);
    }
    if (getOrden() == null || getOrden().compareTo(new Integer("0")) < 0) {
      mostrarMensaje("Campo  requerido y mayor a 0", true);
    }
    return sinerrores;
  }

  @SuppressWarnings("unchecked")
  public void guardarNuevoRegistro() {

    if (validaCamposAlta()) {
      log.info("Guardando registro");
      try {
        getTabla().setId(new FiCfdFoliosId(getFechaAutorizacion(), getNoAutorizacion()));
        getTabla().setPrimerFolio(getPrimerFolio());
        getTabla().setUltimoFolio(getUltimoFolio());
        getTabla().setFoliosUtilizados(new BigDecimal("0"));
        getTabla().setFoliosDiponibles(getUltimoFolio());
        getTabla().setPorcentajeFoliosDisponibles(new BigDecimal("100"));
        getTabla().setFoliosCancelados(new BigDecimal("0"));
        getTabla().setProducto(getProducto());
        getTabla().setSerie(getSerie());
        getTabla().setOrden(getOrden());
        genericService.save(getTabla());
        setListaTabla(genericService.get("FiCfdFolios", "", " order by id.fechaAutorizacion asc "));

        reservaFolios();

        setPaginatorVisible(listaTabla.size() > 6);
        mostrarMensaje("Folios reservados correctamente", false);
        log.info("Folios reservados correctamente");

        setTabla(null);
        setPopUpAlta(false);

      } catch (DataIntegrityViolationException ex) {
        log.error(ex.getMessage(), ex);
        mostrarMensaje("Existe un registro con la misma informacion", true);
      } catch (Exception ex) {
        mostrarMensaje("Ocurrio un error al intentar guardar ", true);
        log.error("Error Guardando registro " + ex.getMessage(), ex);
        Utilerias.guardarMensajeLog("FoliosFiscales", "guardarNuevoRegistro", ex,
            "Error Guardando registro", usuarioLogueado);
      }
    } else {

      mostrarMensaje("Error al validar informaciòn", true);
    }

  }

  public void reservaFolios() {

    try {
      log.info("PReservando Folios");
      for (int i = getTabla().getPrimerFolio().intValue(); i <= getTabla().getUltimoFolio().intValue() - 1; i++) {
        setTablaHistorico(new FiCfdHistoricoFolios());
        getTablaHistorico().setId(new FiCfdHistoricoFoliosId(getFechaAutorizacion(), getNoAutorizacion(), new BigDecimal(i)));
        getTablaHistorico().setStatusFolio("DISPONIBLE");
        getTablaHistorico().setProducto(getProducto());
        getTablaHistorico().setSerie(getSerie());
        getTablaHistorico().setAdicionadoPor(usuarioLogueado.getUsuario());
        getTablaHistorico().setFechaAdicion(new Date());
        genericService.save(getTablaHistorico());
      }
    } catch (Exception ex) {
      mostrarMensaje("Error al Insertar Folio en cfd_historico_folios", true);
      log.error("Error Reservando Folios " + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("FoliosFiscales", "reservaFolios", ex,
          "Error Reservando Folios", usuarioLogueado);
    }
  }
}
