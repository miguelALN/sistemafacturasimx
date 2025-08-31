package com.bancomext.web.beans.pm;

import com.bancomext.service.GenericService;
import com.bancomext.service.ProcedimientosService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiFiuNegativoReclImxTmp;
import com.bancomext.service.mapping.FiParametrizacionImx;
import com.bancomext.service.mapping.FiProcesoAutManImx;
import com.bancomext.service.mapping.VoSumasMontoSsivac;
import com.bancomext.web.utils.Constantes;
import com.bancomext.web.utils.Utilerias;
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
import java.util.ArrayList;
import java.util.List;

@Data
@ManagedBean(name = "reclasificarSaldoNeg")
@ViewScoped
public class ReclasificarSaldoNeg implements Serializable {

  private static final Logger log = LogManager.getLogger(ReclasificarSaldoNeg.class);
  private final GenericService genericService = ServiceLocator.getGenericService();
  private BigDecimal montoAbonoSSivac;
  private BigDecimal montoCargoSSivac;
  private BigDecimal montoOrigenSSivac;
  private BigDecimal sumMontoAbonoSsivac;
  private BigDecimal sumMontoCargoSsivac;
  private BigDecimal sumMontoOrigenSsivac;
  private FiFiuNegativoReclImxTmp beanNegRecla;
  private FiFiuNegativoReclImxTmp beanNegReclaResp;
  private DataTable htmlDataTable;
  private Integer id;
  private List<FiFiuNegativoReclImxTmp> listaProcesoReclasificacionNeg;
  private List<FiParametrizacionImx> listaParametrizacionReclasificar;
  private List<FiProcesoAutManImx> listaProcAutMan;
  private List<VoSumasMontoSsivac> listaSumasMontos;
  private UsuarioDTO usuarioLogueado;
  private String campoA;
  private String cuentaAbono;
  private String cuentaCargo;
  private String cuentaOrigen;
  private String nomCliente;
  private String styleText;
  private UIComponent btnActualiza;
  private UIComponent btnAgregar;
  private UIComponent btnElimina;
  private UIComponent btnErrorAgregar;
  private UIComponent btnErrorProc;
  private UIComponent btnProcedimiento;
  private boolean btnAltaEdita;
  private boolean diferencia;
  private boolean paginatorVisible;
  private boolean popUpAltaEdita; // PARA POP UP ALTA Y MODIFICACION DE REGISTROS
  private int defaultRows = Constantes.REGISTROS;

  public ReclasificarSaldoNeg() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("ProcesoManReclasificarSaldoNegativo", usuarioLogueado.getRol());
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
  public void init() {
    setListaParametrizacionReclasificar(
        genericService.get("FiParametrizacionImx", " TIPO_PROCESO = 'FIU'", " order by 1"));
    setListaProcAutMan(
        genericService.get("FiProcesoAutManImx", " TIPO_PROCESO = 'RCL' ", ""));

    if (!listaParametrizacionReclasificar.isEmpty()) {
      if (listaProcAutMan.get(0).getEjecucion() != null) {
        if (listaProcAutMan.get(0).getEjecucion().equals("M")) {
          try {
            String error = ProcedimientosService.pFiuNegReclManualCalcula();
            if (error == null || error.isEmpty()) {
              mostrarMensaje("El proceso se ejecuto satisfactoriamente", false);
              sumaMontos();
              setPaginatorVisible(listaProcesoReclasificacionNeg.size() > 16);
            } else {
              mostrarMensaje(error, true);
            }
          } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            mostrarMensaje("El procedimiento no se ejecuto satisfactoriamente", true);
          }
        } else {
          mostrarMensaje("El proceso se ha detectado como Autom&#225;tico. No puede operar un proceso MANUAL", true);
        }
      } else {
        mostrarMensaje("No se encontro ningun tipo de Proceso [Manual o Autom&#225;tico] - Por favor verifique la Ejecucion Proceso", true);
      }

    } else {
      mostrarMensaje("Por favor verifique la parametrizaci&#243;n FIU. No se encontraron registros", true);
    }
  }

  public void eliminarAjuste() {

    int rowIndex;
    FiFiuNegativoReclImxTmp bean;
    try {
      log.info("Eliminar Ajuste");
      rowIndex = Integer.parseInt(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("rowIndex"));
      bean = getListaProcesoReclasificacionNeg().get(rowIndex);
      setBeanNegRecla(bean);
      genericService.delete(getBeanNegRecla());
      sumaMontos();
      mostrarMensaje("Registro eliminado satisfactorimente", false);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
  }

  public void muestraEdicion() {
    setPopUpAltaEdita(true);
    setBtnAltaEdita(false);
    int rowIndex;
    try {
      rowIndex = Integer.parseInt(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("rowIndex"));
      beanNegReclaResp = getListaProcesoReclasificacionNeg().get(rowIndex);
      setId(beanNegReclaResp.getId());
      setCampoA(beanNegReclaResp.getCampoA());
      setNomCliente(beanNegReclaResp.getNomCliente());
      setCuentaOrigen(beanNegReclaResp.getCuentaOrigen());
      setMontoOrigenSSivac(beanNegReclaResp.getMontoOrigensSivac());
      setCuentaCargo(beanNegReclaResp.getCuentaCargo());
      setMontoCargoSSivac(beanNegReclaResp.getMontoCargosSivac());
      setCuentaAbono(beanNegReclaResp.getCuentaAbono());
      setMontoAbonoSSivac(beanNegReclaResp.getMontoAbonosSivac());

    } catch (Exception ex) {
      setPopUpAltaEdita(false);
      log.error(ex.getMessage(), ex);
    }
  }

  public void actualizaExtemporaneas() {
    if (sumMontoCargoSsivac.equals(sumMontoAbonoSsivac)) {
      try {
        log.info("EJECUTA PROCESO ---> PKG_ACCION_CONTABLE_MANUAL_P_FIU_NEG_RECL_MANUAL_CAMBIOS");
        String error = ProcedimientosService.pFiuNegReclManualCambios();
        if (error == null || error.isEmpty()) {
          mostrarMensaje("El proceso se ejecuto con &#233;xito.", false);
        } else {
          mostrarMensaje(error, true);
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    } else {
      mostrarMensaje("No se ha ejecutado el proceso. Las sumas de los montos no coinciden", true);
    }
  }

  @SuppressWarnings("unchecked")
  public void modificaAjuste() {
    log.info("MODIFICA AJUSTE");
    beanNegRecla = new FiFiuNegativoReclImxTmp();
    beanNegRecla.setId(getId());
    beanNegRecla.setCampoA(getCampoA());
    beanNegRecla.setNomCliente(getNomCliente());
    beanNegRecla.setCuentaOrigen(getCuentaOrigen());
    beanNegRecla.setMontoOrigensSivac(getMontoOrigenSSivac());
    beanNegRecla.setCuentaCargo(getCuentaCargo());
    beanNegRecla.setMontoCargosSivac(getMontoOrigenSSivac());
    beanNegRecla.setCuentaAbono(getCuentaAbono());
    beanNegRecla.setMontoAbonosSivac(getMontoOrigenSSivac());

    try {
      genericService.update(beanNegRecla);
      clear();
      closePanel();
      setListaProcesoReclasificacionNeg(genericService.get("FiFiuNegativoReclImxTmp", "", " order by 1"));
      sumaMontos();
      mostrarMensaje("Se ha actualiz&#243; correctamente el registro", false);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  public void sumaMontos() {
    setListaProcesoReclasificacionNeg(genericService.get("FiFiuNegativoReclImxTmp", "", " ORDER BY CUENTA_ORIGEN, CAMPOA"));
    listaSumasMontos = new ArrayList<>();
    VoSumasMontoSsivac beanSumas = new VoSumasMontoSsivac();
    sumMontoOrigenSsivac = new BigDecimal("0.0");
    sumMontoCargoSsivac = new BigDecimal("0.0");
    sumMontoAbonoSsivac = new BigDecimal("0.0");
    for (FiFiuNegativoReclImxTmp fiFiuNegativoReclImxTmp : listaProcesoReclasificacionNeg) {

      if (fiFiuNegativoReclImxTmp.getMontoOrigensSivac() != null) {
        sumMontoOrigenSsivac = sumMontoOrigenSsivac.add(fiFiuNegativoReclImxTmp.getMontoOrigensSivac());
      }
      if (fiFiuNegativoReclImxTmp.getMontoCargosSivac() != null) {
        sumMontoCargoSsivac = sumMontoCargoSsivac.add(fiFiuNegativoReclImxTmp.getMontoCargosSivac());
      }
      if (fiFiuNegativoReclImxTmp.getMontoAbonosSivac() != null) {
        sumMontoAbonoSsivac = sumMontoAbonoSsivac.add(fiFiuNegativoReclImxTmp.getMontoAbonosSivac());
      }
      if (fiFiuNegativoReclImxTmp.getMontoCargosSivac() != null) {
        diferencia = !fiFiuNegativoReclImxTmp.getMontoCargosSivac().equals(fiFiuNegativoReclImxTmp.getMontoAbonosSivac());
      }

      fiFiuNegativoReclImxTmp.setBandera(diferencia);
      fiFiuNegativoReclImxTmp.setMontoOriFor(Utilerias.formatearNumero(fiFiuNegativoReclImxTmp.getMontoOrigensSivac(), ""));
      fiFiuNegativoReclImxTmp.setMontoCarFor(Utilerias.formatearNumero(fiFiuNegativoReclImxTmp.getMontoCargosSivac(), ""));
      fiFiuNegativoReclImxTmp.setMontoAboFor(Utilerias.formatearNumero(fiFiuNegativoReclImxTmp.getMontoAbonosSivac(), ""));

    }

    beanSumas.setMontoOrigen(Utilerias.formatearNumero(sumMontoOrigenSsivac, ""));
    beanSumas.setMontoCargo(Utilerias.formatearNumero(sumMontoCargoSsivac, ""));
    beanSumas.setMontoAbono(Utilerias.formatearNumero(sumMontoAbonoSsivac, ""));

    listaSumasMontos.add(beanSumas);

    if (sumMontoCargoSsivac.equals(sumMontoAbonoSsivac)) {
      styleText = "background-color: #B2FF66;";
    } else {
      styleText = "background-color: #FF3333;";
    }
  }

  public void openPanel() {
    setPopUpAltaEdita(true);
    setBtnAltaEdita(true);
  }

  public void closePanel() {
    setPopUpAltaEdita(false);
    clear();
  }

  public void clear() {
    setId(0);
    setCampoA(null);
    setNomCliente(null);
    setCuentaOrigen(null);
    setMontoOrigenSSivac(null);
    setCuentaCargo(null);
    setMontoCargoSSivac(null);
    setCuentaAbono(null);
    setMontoAbonoSSivac(null);
  }
}
