package com.bancomext.web.beans.pm;

import com.bancomext.service.GenericService;
import com.bancomext.service.ProcedimientosService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiFiuNegativoRestImxTmp;
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
@ManagedBean(name = "restaurarSaldoNeg")
@ViewScoped
public class RestaurarSaldoNeg implements Serializable {

  private static final Logger log = LogManager.getLogger(RestaurarSaldoNeg.class);
  private final GenericService genericService = ServiceLocator.getGenericService();
  private BigDecimal montoAbonoSSivac;
  private BigDecimal montoCargoSSivac;
  private BigDecimal montoOrigenSSivac;
  private BigDecimal sumMontoAbonoSsivac;
  private BigDecimal sumMontoCargoSsivac;
  private BigDecimal sumMontoOrigenSsivac;
  private FiFiuNegativoRestImxTmp beanRest;
  private FiFiuNegativoRestImxTmp beanRestResp;
  private DataTable htmlDataTable;
  private Integer id;
  private List<FiFiuNegativoRestImxTmp> listaProcesoRestauracionNeg;
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

  public RestaurarSaldoNeg() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("ProcesoManRestaurarSaldoNegativo", usuarioLogueado.getRol());
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
    //setListParametrizacionReclasificar(genericService.get("FiParametrizacionImx","OPCODE = 'VF_KL_FUC_COF' AND TIPO_PROCESO = 'REC'"," order by 1"));
    setListaParametrizacionReclasificar(
        genericService.get("FiParametrizacionImx", " TIPO_PROCESO = 'FIU'", " order by 1"));
    setListaProcAutMan(
        genericService.get("FiProcesoAutManImx", " TIPO_PROCESO = 'RES' ", ""));

    if (!listaParametrizacionReclasificar.isEmpty()) {
      if (listaProcAutMan.get(0).getEjecucion() != null) {
        if (listaProcAutMan.get(0).getEjecucion().equals("M")) {
          try {
            String error;
            error = ProcedimientosService.pFiuNegRestManualCalcula();

            if (error == null || error.isEmpty()) {
              sumaMontos();
              setPaginatorVisible(listaProcesoRestauracionNeg.size() > 16);
            } else {
              mostrarMensaje(error, true);
            }
          } catch (Exception e) {
            log.error(e.getMessage(), e);
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

  @SuppressWarnings("unchecked")
  public void modificaAjuste() {
    log.info("MODIFICA AJUSTE");
    beanRest = new FiFiuNegativoRestImxTmp();
    beanRest.setId(getId());
    beanRest.setCampoA(getCampoA());
    beanRest.setNomCliente(getNomCliente());
    beanRest.setCuentaOrigen(getCuentaOrigen());
    beanRest.setMontoOrigensSivac(getMontoOrigenSSivac());
    beanRest.setCuentaCargo(getCuentaCargo());
    beanRest.setMontoCargosSivac(getMontoOrigenSSivac());
    beanRest.setCuentaAbono(getCuentaAbono());
    beanRest.setMontoAbonosSivac(getMontoOrigenSSivac());
    try {
      genericService.update(beanRest);
      clear();
      closePanel();
      setListaProcesoRestauracionNeg(genericService.get("FiFiuNegativoRestImxTmp", "", " order by 1"));
      sumaMontos();
      mostrarMensaje("Se ha actualiz&#243; correctamente el registro", false);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  public void actualizaExtemporaneas() {
    String error;

    if (sumMontoCargoSsivac.equals(sumMontoAbonoSsivac)) {
      try {
        log.info("EJECUTA PROCESO ---> PKG_ACCION_CONTABLE_MANUAL_P_FIU_NEG_RECL_MANUAL_CAMBIOS");
        error = ProcedimientosService.pFiuNegRestManualCambios();
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

  public void muestraEdicion() {
    setPopUpAltaEdita(true);
    setBtnAltaEdita(false);
    int rowIndex;
    try {
      rowIndex = Integer.parseInt(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("rowIndex"));
      beanRestResp = getListaProcesoRestauracionNeg().get(rowIndex);
      setId(beanRestResp.getId());
      setCampoA(beanRestResp.getCampoA());
      setNomCliente(beanRestResp.getNomCliente());
      setCuentaOrigen(beanRestResp.getCuentaOrigen());
      setMontoOrigenSSivac(beanRestResp.getMontoOrigensSivac());
      setCuentaCargo(beanRestResp.getCuentaCargo());
      setMontoCargoSSivac(beanRestResp.getMontoCargosSivac());
      setCuentaAbono(beanRestResp.getCuentaAbono());
      setMontoAbonoSSivac(beanRestResp.getMontoAbonosSivac());
    } catch (Exception ex) {
      setPopUpAltaEdita(false);
      log.error(ex.getMessage(), ex);
    }
  }

  public void eliminarAjuste() {

    int rowIndex;
    FiFiuNegativoRestImxTmp bean;
    try {
      log.info("Eliminar Ajuste");
      rowIndex = Integer.parseInt(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("rowIndex"));
      bean = getListaProcesoRestauracionNeg().get(rowIndex);
      setBeanRest(bean);
      genericService.delete(getBeanRest());
      sumaMontos();
      mostrarMensaje("Registro eliminado Satisfactorimente", true);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
  }

  @SuppressWarnings("unchecked")
  public void sumaMontos() {
    try {
      setListaProcesoRestauracionNeg(genericService.get("FiFiuNegativoRestImxTmp", "", " order by CUENTA_ORIGEN , CAMPOA, MONTO_CARGO_SSICAV DESC"));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    listaSumasMontos = new ArrayList<>();
    VoSumasMontoSsivac beanSumas = new VoSumasMontoSsivac();
    sumMontoOrigenSsivac = new BigDecimal("0.0");
    sumMontoCargoSsivac = new BigDecimal("0.0");
    sumMontoAbonoSsivac = new BigDecimal("0.0");
    for (FiFiuNegativoRestImxTmp fiFiuNegativoRestImxTmp : listaProcesoRestauracionNeg) {

      if (fiFiuNegativoRestImxTmp.getMontoOrigensSivac() != null) {
        sumMontoOrigenSsivac = sumMontoOrigenSsivac.add(fiFiuNegativoRestImxTmp.getMontoOrigensSivac());
      }
      if (fiFiuNegativoRestImxTmp.getMontoCargosSivac() != null) {
        sumMontoCargoSsivac = sumMontoCargoSsivac.add(fiFiuNegativoRestImxTmp.getMontoCargosSivac());
      }
      if (fiFiuNegativoRestImxTmp.getMontoAbonosSivac() != null) {
        sumMontoAbonoSsivac = sumMontoAbonoSsivac.add(fiFiuNegativoRestImxTmp.getMontoAbonosSivac());
      }
      if (fiFiuNegativoRestImxTmp.getMontoCargosSivac() != null) {
        diferencia = !fiFiuNegativoRestImxTmp.getMontoCargosSivac().equals(fiFiuNegativoRestImxTmp.getMontoAbonosSivac());
      }

      fiFiuNegativoRestImxTmp.setBandera(diferencia);
      fiFiuNegativoRestImxTmp.setMontoOriFor(Utilerias.formatearNumero(fiFiuNegativoRestImxTmp.getMontoOrigensSivac(), ""));
      fiFiuNegativoRestImxTmp.setMontoCarFor(Utilerias.formatearNumero(fiFiuNegativoRestImxTmp.getMontoCargosSivac(), ""));
      fiFiuNegativoRestImxTmp.setMontoAboFor(Utilerias.formatearNumero(fiFiuNegativoRestImxTmp.getMontoAbonosSivac(), ""));
    }

    if (sumMontoCargoSsivac.equals(sumMontoAbonoSsivac)) {
      styleText = "background-color: #B2FF66;";
    } else {
      styleText = "background-color: #FF3333;";
    }

    sumMontoOrigenSsivac = sumMontoOrigenSsivac.divide(new BigDecimal("2.0"));

    beanSumas.setMontoOrigen(Utilerias.formatearNumero(sumMontoOrigenSsivac, ""));
    beanSumas.setMontoCargo(Utilerias.formatearNumero(sumMontoCargoSsivac, ""));
    beanSumas.setMontoAbono(Utilerias.formatearNumero(sumMontoAbonoSsivac, ""));

    listaSumasMontos.add(beanSumas);
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
