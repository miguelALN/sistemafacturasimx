package com.bancomext.web.beans.pm;

import com.bancomext.service.GenericService;
import com.bancomext.service.ProcedimientosService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiCancelacionImxTmp;
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
@ManagedBean(name = "cancelProvisionInteres")
@ViewScoped
public class CancelProvisionInteres implements Serializable {

  private static final Logger log = LogManager.getLogger(CancelProvisionInteres.class);
  private final GenericService genericService = ServiceLocator.getGenericService();
  private BigDecimal montoAbonoSSivac;
  private BigDecimal montoCargoSSivac;
  private BigDecimal montoOrigenSSivac;
  private BigDecimal sumMontoAbonoSsivac;
  private BigDecimal sumMontoCargoSsivac;
  private DataTable htmlDataTable;
  private Integer ctaOrigen;
  private Integer sctaOrigen;
  private Integer ssCtaOrigen;
  private Integer sssCtaOrigen;
  private Integer ctaCargo;
  private Integer sctaCargo;
  private Integer ssCtaCargo;
  private Integer sssCtaCargo;
  private Integer ctaAbono;
  private Integer sctaAbono;
  private Integer ssCtaAbono;
  private Integer sssCtaAbono;
  private Integer id;
  private List<FiCancelacionImxTmp> listaProcCancelacion;
  private List<FiParametrizacionImx> listaCancelacionProvision;
  private List<FiProcesoAutManImx> listaProcAutMan;
  private List<VoSumasMontoSsivac> listaSumasMontos;
  private UsuarioDTO usuarioLogueado;
  private String campoA;
  private String cuentaAbono;
  private String cuentaCargo;
  private String cuentaOrigen;
  private String mensaje;
  private String nomCliente;
  private String styleText;
  private UIComponent btnActualiza;
  private UIComponent btnAgregar;
  private UIComponent btnElimina;
  private boolean btnUpdateSaveInt;
  private boolean diferencia;
  private boolean paginatorVisible;
  private boolean procCanCapInt; // PARA POP UP ALTA Y MODIFICACION DE REGISTROS
  private int defaultRows = Constantes.REGISTROS;

  public CancelProvisionInteres() {
    log.info("INTO CancelProvisionInteres");
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("ProcesoManCancelacionProvIntereses", usuarioLogueado.getRol());
    }
  }

  // METODO GENERICO PARA CREAR MENSAJES
  private static void mostrarMensaje(final String msg, final boolean esError) {

      log.info("mostrarMensaje autoupdate " + msg);
      final FacesMessage mensaje = new FacesMessage();
      mensaje.setSummary(msg);
      mensaje.setDetail(msg);
      mensaje.setSeverity(esError ? FacesMessage.SEVERITY_ERROR : FacesMessage.SEVERITY_INFO);
      FacesContext.getCurrentInstance().addMessage(null, mensaje);
      log.info("BEFORE MESSAGES");
      PrimeFaces.current().ajax().update("form1:messages");
      log.info("AFTER MESSAGES");

  }


  @SuppressWarnings("unchecked")
  public void init() {
    setListaCancelacionProvision(
        genericService.get("FiParametrizacionImx", " TIPO_PROCESO = 'CAN'", " order by 1"));
    setListaProcAutMan(
        genericService.get("FiProcesoAutManImx", " TIPO_PROCESO = 'CAN' ", ""));

    if (!listaCancelacionProvision.isEmpty()) {
      if (listaProcAutMan.get(0).getEjecucion() != null) {
        if (listaProcAutMan.get(0).getEjecucion().equals("M")) {
          try {
            String error = ProcedimientosService.pCanProvIntManualCalcula();
            if (error == null || error.isEmpty()) {
              mostrarMensaje("El proceso se ejecut&#243; satisfactoriamente", false);
              setListaProcCancelacion(genericService.get(
                  "FiCancelacionImxTmp", " ", " ORDER BY FLAG, CUENTA_ORIGEN, CAMPOA, NUMPOL"));
              sumaMontos();
              setPaginatorVisible(listaProcCancelacion.size() > 16);
            } else {
              mostrarMensaje(error, true);
            }
          } catch (Exception ex) {
            log.info("GOINTO SEND MESSATE");
            mostrarMensaje("El procedimiento no se ejecut&#243; satisfactoriamente", true);
          }
        } else {
          mostrarMensaje("El proceso se ha detectado como Autom&#225;tico. No puede operar un proceso MANUAL",
              true);
        }
      } else {
        mostrarMensaje(
            "No se encontro ningun tipo de Proceso [Manual o Autom&#225;tico] - Por favor verifique la Ejecucion Proceso",
            true);
      }
    } else {
      mostrarMensaje(
          "Por favor verifique la parametrizaci&#243;n de la Cancelaci&#243;n de la Provisi&#243;n de Inter&#233;ses. No se encontraron registros",
          true);
    }
  }

  @SuppressWarnings("unchecked")
  public void sumaMontos() {
    listaSumasMontos = new ArrayList<>();
    VoSumasMontoSsivac beanSumas = new VoSumasMontoSsivac();
    BigDecimal sumMontoOrigenSsivac = new BigDecimal("0.0");
    sumMontoCargoSsivac = new BigDecimal("0.0");
    sumMontoAbonoSsivac = new BigDecimal("0.0");

    // CONSULTA PARA SUMAR LOS MONTOS DE CANCELACION DIFERENTES A 0
    List<FiCancelacionImxTmp> listaMontoCargoAbono = (genericService.get("FiCancelacionImxTmp", " FLAG = 1", " ORDER BY FLAG,CAMPOA,CUENTA_ORIGEN"));

    // SUMA DE MONTO ORIGEN SICAV
    for (FiCancelacionImxTmp fiCancelacionImxTmp : listaProcCancelacion) {
      //if(listaProcCancelacion.get(i).getCampoA() != null) {  ELOY COMENTO QUE SE QUITARA CONDICIONAL 25-06-2015

      if (fiCancelacionImxTmp.getMontoOrigenSSivac() != null) {
        sumMontoOrigenSsivac = sumMontoOrigenSsivac.add(fiCancelacionImxTmp.getMontoOrigenSSivac());
      }

      fiCancelacionImxTmp.setMontoOriFor(Utilerias.formatearNumero(fiCancelacionImxTmp.getMontoOrigenSSivac(), ""));
      fiCancelacionImxTmp.setMontoCarFor(Utilerias.formatearNumero(fiCancelacionImxTmp.getMontoCargoSSivac(), ""));
      fiCancelacionImxTmp.setMontoAbonoSicavFor(Utilerias.formatearNumero(fiCancelacionImxTmp.getMontoAbonoSSivac(), ""));

      if (fiCancelacionImxTmp.getMontoAbono() != null) {
        fiCancelacionImxTmp.setMontoAbonoOrigFor(Utilerias.formatearNumero(fiCancelacionImxTmp.getMontoAbono(), ""));
      }
    }

    // SUMA MONTO CARGO ABONO
    for (int j = 0; j < listaMontoCargoAbono.size(); j++) {

      if (listaMontoCargoAbono.get(j).getMontoCargoSSivac() != null) {
        sumMontoCargoSsivac = sumMontoCargoSsivac.add(listaMontoCargoAbono.get(j).getMontoCargoSSivac());
      }
      if (listaMontoCargoAbono.get(j).getMontoAbonoSSivac() != null) {
        sumMontoAbonoSsivac = sumMontoAbonoSsivac.add(listaMontoCargoAbono.get(j).getMontoAbonoSSivac());
      }

      // COMPARA MONTOS PARA INDICAR VALORES CORRECTOS (IMAGEN DE CORRECTO EN LA VISTA)
      if (listaMontoCargoAbono.get(j).getMontoCargoSSivac() != null && listaMontoCargoAbono.get(j).getMontoAbonoSSivac() != null) {
        diferencia = !listaMontoCargoAbono.get(j).getMontoCargoSSivac().equals(listaMontoCargoAbono.get(j).getMontoAbonoSSivac());
        listaProcCancelacion.get(j).setBandera(diferencia);
      }

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

  public void actualizaExtemporaneas() {
    String error;
    if ( sumMontoCargoSsivac != null && sumMontoCargoSsivac.equals(sumMontoAbonoSsivac)) {
      try {
        log.info("EJECUTA PROCESO ---> PKG_ACCION_CONTABLE_MANUAL_P_CAN_PROV_INT_MANUAL_CAMBIOS");
        error = ProcedimientosService.pCanProvIntManualCambios();
        if (error == null || error.isEmpty()) {
          mostrarMensaje("El proceso se ejecuto con &#233;xito.", false);
        } else {
          mostrarMensaje(error, true);
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        mostrarMensaje("El proceso se ejecuto con &#233;xito.", false);
      }
    } else {
      log.info("error null");
      mostrarMensaje("No se ha ejecutado el proceso. Las sumas de los montos no coinciden", true);
    }
  }

  public void muestraEdicion() {
    setProcCanCapInt(true);
    setBtnUpdateSaveInt(false);
    int rowIndex;
    try {
      rowIndex = Integer.parseInt(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("rowIndex"));
      FiCancelacionImxTmp beanCancelacionResp = getListaProcCancelacion().get(rowIndex);
      setId(beanCancelacionResp.getId());
      setCampoA(beanCancelacionResp.getCampoA());
      setNomCliente(beanCancelacionResp.getNomCliente());
      setCuentaOrigen(beanCancelacionResp.getCuentaOrigen());
      setMontoOrigenSSivac(beanCancelacionResp.getMontoOrigenSSivac());
      setCuentaCargo(beanCancelacionResp.getCuentaCargo());
      setMontoCargoSSivac(beanCancelacionResp.getMontoCargoSSivac());
      setCuentaAbono(beanCancelacionResp.getCuentaAbono());
      setMontoAbonoSSivac(beanCancelacionResp.getMontoAbonoSSivac());
      setCtaOrigen(beanCancelacionResp.getCtaOrigen());
      setSctaOrigen(beanCancelacionResp.getSctaOrigen());
      setSsCtaOrigen(beanCancelacionResp.getSsctaOrigen());
      setSssCtaOrigen(beanCancelacionResp.getSssctaOrigen());
      setCtaCargo(beanCancelacionResp.getCtaCargo());
      setSctaCargo(beanCancelacionResp.getSctaCargo());
      setSsCtaCargo(beanCancelacionResp.getSsctaCargo());
      setSssCtaCargo(beanCancelacionResp.getSssctaCargo());
      setCtaAbono(beanCancelacionResp.getCtaAbono());
      setSctaAbono(beanCancelacionResp.getSctaAbono());
      setSsCtaAbono(beanCancelacionResp.getSsctaAbono());
      setSssCtaAbono(beanCancelacionResp.getSssctaAbono());

    } catch (Exception ex) {
      setProcCanCapInt(false);
      log.error(ex.getMessage(), ex);
    }
  }

  @SuppressWarnings("unchecked")
  public void modificaAjuste() {
    log.info("MODIFICA AJUSTE");
    FiCancelacionImxTmp beanCancelacion = new FiCancelacionImxTmp();
    beanCancelacion.setId(getId());
    beanCancelacion.setCampoA(getCampoA());
    beanCancelacion.setNomCliente(getNomCliente());
    beanCancelacion.setCuentaOrigen(getCuentaOrigen());
    beanCancelacion.setMontoOrigenSSivac(getMontoOrigenSSivac());
    beanCancelacion.setCuentaCargo(getCuentaCargo());
    beanCancelacion.setMontoCargoSSivac(getMontoOrigenSSivac());
    beanCancelacion.setCuentaAbono(getCuentaAbono());
    beanCancelacion.setMontoAbonoSSivac(getMontoOrigenSSivac());

    beanCancelacion.setCtaOrigen(getCtaOrigen());
    beanCancelacion.setSctaOrigen(getSctaOrigen());
    beanCancelacion.setSsctaOrigen(getSsCtaOrigen());
    beanCancelacion.setSssctaOrigen(getSssCtaOrigen());

    beanCancelacion.setCtaCargo(getCtaCargo());
    beanCancelacion.setSctaCargo(getSctaCargo());
    beanCancelacion.setSsctaCargo(getSsCtaCargo());
    beanCancelacion.setSssctaCargo(getSssCtaCargo());

    beanCancelacion.setCtaAbono(getCtaAbono());
    beanCancelacion.setCtaAbono(getSctaAbono());
    beanCancelacion.setSsctaAbono(getSsCtaAbono());
    beanCancelacion.setSssctaAbono(getSssCtaAbono());

    beanCancelacion.setSuCcon(0);
    beanCancelacion.setDepto(19);
    beanCancelacion.setOpCode("VC_KL_FUC_COF");
    beanCancelacion.setNumPol(500);
    beanCancelacion.setMoneda(1);
    beanCancelacion.setMontoCargo(null);
    beanCancelacion.setMontoAbono(null);
    beanCancelacion.setFeCad(31);
    beanCancelacion.setFeCam(5);
    beanCancelacion.setFeCaa(2015);

    try {
      genericService.update(beanCancelacion);
      clear();
      closePanel();
      setListaProcCancelacion(genericService.get("FiCancelacionImxTmp", "", " order by 1"));
      sumaMontos();
      mostrarMensaje("Se ha actualiz&#243; correctamente el registro", false);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
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

  public void openPanel() {
    setProcCanCapInt(true);//
    setBtnUpdateSaveInt(true);
  }

  public void closePanel() {
    setProcCanCapInt(false);
    clear();
  }

}
