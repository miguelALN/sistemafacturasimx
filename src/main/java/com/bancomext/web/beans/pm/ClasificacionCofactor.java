package com.bancomext.web.beans.pm;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ProcedimientosService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.*;
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
@ManagedBean(name = "clasificacionCofactor")
@ViewScoped
public class ClasificacionCofactor implements Serializable {

  private static final Logger log = LogManager.getLogger(ClasificacionCofactor.class);
  private final GenericService genericService = ServiceLocator.getGenericService();
  private BigDecimal e102142;
  private BigDecimal saldoSicav;
  private BigDecimal sumMontoCofactor;
  private BigDecimal sumMontoSicav;
  private FiCofactoresImx beanCofactores;
  private FiCofactoresImxDetTmp bean;
  private DataTable htmlDataTable1; // Para acceder a la tabla de los saldo de cofactor
  private DataTable htmlDataTable; // Para acceder a los elementos de la tabla principal
  private List<FiCofactoresImxDetTmp> listaDetalleCofactores; // Lista para visualizar la lista de los saldo de los cofactores
  private List<FiParametrizacionImx> listaParamCofactor; // Lista para verificar que existan datos en la parametrizacion
  private List<FiProcesoAutManImx> listaProcAutMan; // Lista para verificar que existan datos en la ejecucion de tipo de proceso
  private List<VoCofactoresTmp> listaClasificacionCofactor; // lista para mostrar los datos de la tabla temporal
  private List<VoSumasMontoSsivac> listaSumaMontos;  // lista para agregar la suma de los montos
  private List<VoTablaElemento> listaCofactoresFaltantes;
  private UsuarioDTO usuarioLogueado;
  private String campoA;
  private String campoB;
  private String contratoImx;
  private String cuentaOrigen;
  private String error;
  private String mensaje1;
  private String mensaje;
  private String nomCliente;
  private String nombreCofactor;
  private String rowIndex;
  private String rowIndexCof;
  private String saldoCofactor;
  private String saldoSicavCof;
  private String styleText;
  private UIComponent btnActualiza; // boton para mostrar mensaje que se ha actualizado correctamente la tabla
  private UIComponent btnAgregar; // boton para mostrar mensaje que se ha insertado satisfactorimente
  private UIComponent btnElimina; // boton para mostrar que se eliminado correctamente el registro
  private UIComponent btnError;
  private UIComponent btnErrorProc; // boton para mostrar que el procedimiento no se ejecuto satisfactoriamente
  private UIComponent btnProcedimiento; // boton para mostrar que el procedimiento se ha ejecutado satisfactoriamente
  private boolean btnAltaEdita;
  private boolean btnEjecutarProceso;
  private boolean paginatorVisible; // PARA MOSTRAR EL PAGINADOR
  private boolean popUpAltaCofactores;
  private boolean popUpAltaEdita; // PARA POP UP ALTA Y MODIFICACION DE REGISTROS
  private boolean popUpAltaEditaCof; // PARA POP UP ALTA Y MODIFICACION DE REGISTROS
  private int defaultRows = Constantes.REGISTROS;
  private int id;

  public ClasificacionCofactor() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("ProcesoManClasificacionDeCofactor", usuarioLogueado.getRol());
    }
    init();
  }

  // METODO GENERICO PARA CREAR MENSAJES
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
    final List<VoTablaElemento> cofactoresFaltantes = ConsultasService.getCofactoresFaltantes();
    if (!cofactoresFaltantes.isEmpty()) {
      listaCofactoresFaltantes = cofactoresFaltantes;
      mostrarMensaje("Por favor dar de alta los cofactores", false);
      btnEjecutarProceso = false;
    } else {
      btnEjecutarProceso = true;
      if (listaCofactoresFaltantes != null) {
        listaCofactoresFaltantes.clear();
      }
      setListaParamCofactor(genericService.get("FiParametrizacionImx", " TIPO_PROCESO = 'COF'", " order by 1"));
      setListaProcAutMan(genericService.get("FiProcesoAutManImx", " TIPO_PROCESO = 'COF' ", ""));
      if (!listaParamCofactor.isEmpty()) {
        if (listaProcAutMan.get(0).getEjecucion() != null) {
          if (listaProcAutMan.get(0).getEjecucion().equals("M")) {
            String error;
            error = ProcedimientosService.pCofCampoBManualCalcula();
            if (error == null || error.isEmpty()) {
              try {
                log.info("Se ejecuto procedimiento satisfactoriamente");
                mostrarMensaje("El proceso se ejecut&#243; satisfactoriamente", false);
              } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                mostrarMensaje("El procedimiento no se ejecuto satisfactoriamente", true);
              }
              sumaMontos();
              setPaginatorVisible(listaClasificacionCofactor.size() > 16);
            } else {
              mostrarMensaje(error, true);
            }
          } else {
            mostrarMensaje("El proceso se ha detectado como Autom&#225;tico. No puede operar un proceso MANUAL", true);
          }
        } else {
          mostrarMensaje("No se encontro ningun tipo de Proceso [Manual o Autom&#225;tico] - Por favor verifique la Ejecucion Proceso", true);
        }
      } else {
        mostrarMensaje("Por favor verifique la parametrizaci&#243;n Clasificaci&#243;n Cofactor. No se encontraron registros", true);
      }
    }
  }

  // METODO QUE SE ENCARGA DE LA SUMA DE LOS MONTOS
  public void sumaMontos() {
    VoSumasMontoSsivac beanSumas = new VoSumasMontoSsivac();
    listaClasificacionCofactor = new ArrayList<>();
    listaSumaMontos = new ArrayList<>();
    sumMontoSicav = new BigDecimal("0.0");
    sumMontoCofactor = new BigDecimal("0.0");
    boolean bandera;


    // CONSULTA PARA MOSTRAR REGISTRO EN PANTALLA DE CLASIFICACION DE COFACTOR
    listaClasificacionCofactor = ConsultasService.getClasificacionCofactor();

    // SE APLICA FORMATO PARA LOS MONTOS DE LA CLASIFICACION DE COFACTOR
    for (VoCofactoresTmp voCofactoresTmp : listaClasificacionCofactor) {
      if (voCofactoresTmp.getSaldoSicav() != null) {
        sumMontoSicav = sumMontoSicav.add(voCofactoresTmp.getSaldoSicav());
        voCofactoresTmp.setSaldoSicavFor(Utilerias.formatearNumero(voCofactoresTmp.getSaldoSicav(), ""));
      }
      if (voCofactoresTmp.getE102142() != null) {
        sumMontoCofactor = sumMontoCofactor.add(voCofactoresTmp.getE102142());
        voCofactoresTmp.setE102142For(Utilerias.formatearNumero(voCofactoresTmp.getE102142(), ""));
      }
      if (voCofactoresTmp.getSaldoSicav() != null) {
        bandera = !voCofactoresTmp.getSaldoSicav().equals(voCofactoresTmp.getE102142());
        voCofactoresTmp.setDiferencia(bandera);
      }
    }
    beanSumas.setMontoSicav(Utilerias.formatearNumero(sumMontoSicav, ""));
    beanSumas.setMontoCofactor(Utilerias.formatearNumero(sumMontoCofactor, ""));
    listaSumaMontos.add(beanSumas);

    if (sumMontoSicav.equals(sumMontoCofactor)) {
      styleText = "background-color: #B2FF66;";
    } else {
      styleText = "background-color: #FF3333;";
    }
    setMensaje(null);
  }

  // SE EJECUTA EL PROCESO DE CAMBIOS PARA EJECUTAR TABLAS EXTEMPORANEAS
  public void actualizaExtemporaneas() {
    String error;
    if (sumMontoSicav.equals(sumMontoCofactor)) {
      try {
        log.info("EJECUTA PROCESO ---> PKG_ACCION_CONTABLE_MANUAL_P_COF_CAMPOB_MANUAL_CAMBIOS");
        error = ProcedimientosService.pCofCampoBManualCambios();
        if (error == null || error.isEmpty()) {
          mostrarMensaje("El proceso se ejecuto con &#233;xito.", false);
        } else {
          mostrarMensaje(error, true);
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    } else {
      mostrarMensaje("No se ha ejecutado el proceso. Las sumas de los montos no coinciden", false);
    }
  }

  // METODO QUE OBTIENE EL DETALLE DEL COFACTOR
  @SuppressWarnings("unchecked")
  public void detalleCofactor() {
    setPopUpAltaEdita(true);
    setBtnAltaEdita(false);
    listaDetalleCofactores = new ArrayList<>();
    try {
      // PARAMETROS
      campoA = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("campoA");
      saldoSicavCof = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("saldoSicavFor");
      rowIndex = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("rowIndex");
      String saldoSuma = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("saldoSicav");
      saldoSicav = new BigDecimal(saldoSuma);

      // LISTA DE DETALLE DE COFACTORES
      listaDetalleCofactores = genericService.get("FiCofactoresImxDetTmp", " CAMPOA = '" + campoA + "'", "ORDER BY CAMPOA");

      // SE APLICA FORMATO CON DECIMALES A LOS MONTOS
      for (FiCofactoresImxDetTmp listDetalleCofactore : listaDetalleCofactores) {
        listDetalleCofactore.setE102142SumFormat(Utilerias.formatearNumero(listDetalleCofactore.getE102142Sum(), ""));
      }

      // SE LIMPIA VARIABLE DE MENSJAE
      setMensaje("");
      setMensaje1("");
      setError("");
    } catch (Exception ex) {
      setPopUpAltaEdita(false);
      log.error(ex.getMessage(), ex);
    }
  }

  // METODO QUE ABRE EL PANEL PARA EL AJUSTE DEL COFACTOR SE ENVIAN PARAMETROS
  public void openPanelAjusteCofactor() {
    setPopUpAltaEditaCof(true);
    campoB = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("campoB");
    rowIndexCof = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("rowIndexCof");
    if (listaDetalleCofactores.size() == 1) {
      saldoCofactor = saldoSicavCof;
    } else {
      saldoCofactor = "";
    }
  }

  // SE REALIZA LA MODIFICACION DEL IMPORTE DE COFACTOR
  @SuppressWarnings("unchecked")
  public void modificaMontoCofactor() {
    bean = getListaDetalleCofactores().get(Integer.parseInt(rowIndexCof));

    if (saldoCofactor != null && !saldoCofactor.isEmpty()) {
      saldoCofactor = saldoCofactor.replaceAll(",", "");
      bean.setE102142Sum(new BigDecimal(saldoCofactor));
      try {
        genericService.update(bean);
        closePanelCof();
        sumaMontos();
        mensaje = "Actualizaci&#243;n Correcta.";

        // SE VALIDA QUE LOS MONTOS COFACTORES COINCIDAN CON EL MONTO SICAV
        BigDecimal sumaCofactores = new BigDecimal("0");

        listaDetalleCofactores = genericService.get("FiCofactoresImxDetTmp", " CAMPOA = '" + campoA + "'", "ORDER BY CAMPOA");
        for (FiCofactoresImxDetTmp listDetalleCofactore : listaDetalleCofactores) {
          listDetalleCofactore.setE102142SumFormat(Utilerias.formatearNumero(listDetalleCofactore.getE102142Sum(), ""));
          sumaCofactores = sumaCofactores.add(listDetalleCofactore.getE102142Sum());
        }
        if (sumaCofactores.equals(saldoSicav)) {
          mensaje1 = "Los montos coinciden.";
          setError("");
        } else {
          error = "Los Montos no coinciden. Verifique.";
          setMensaje1("");
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    } else {
      closePanelCof();
      sumaMontos();
      error = "No se ingreso ningun Monto para ajustar Cofactor.";
    }
  }

  // SE CIERRA EL PANEL DE AJUSTE DEL MONTO DE LOS COFACTORES Y REFRESCA LA PANTALLA DE DETALLE
  @SuppressWarnings("unchecked")
  public void closePanelCof() {
    listaDetalleCofactores = genericService.get("FiCofactoresImxDetTmp", " CAMPOA = '" + campoA + "'", "ORDER BY CAMPOA");
    for (FiCofactoresImxDetTmp listDetalleCofactore : listaDetalleCofactores) {
      listDetalleCofactore.setE102142SumFormat(Utilerias.formatearNumero(listDetalleCofactore.getE102142Sum(), ""));
    }
    setPopUpAltaEditaCof(false);
  }

  public void closePanel() {
    setPopUpAltaEdita(false);
  }

  public void altaCofactores() {
    nombreCofactor = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("nombreCofactor");
    setPopUpAltaCofactores(true);
  }

  public void closeAltaCofactores() {
    clearCofactores();
    setPopUpAltaCofactores(false);
  }

  public void agregarCofactor() {
    if (nombreCofactor != null && campoB != null && !nombreCofactor.isEmpty() && !campoB.isEmpty()) {
      campoB = String.format("%08d", Integer.parseInt(campoB));
      try {
        mensaje = ProcedimientosService.pModificaCofactor(campoB, nombreCofactor);
        if (mensaje == null || mensaje.isEmpty()) {
          closeAltaCofactores();
          clearCofactores();
          init();
        } else {
          clearCofactores();
          mostrarMensaje(mensaje, false);
        }
      } catch (Exception ex) {
        mostrarMensaje("El registro ya existe.", true);
        log.error(ex.getMessage(), ex);
      }
    } else {
      mostrarMensaje("Todos los campos son requeridos por favor verifique a intente de nuevo", true);
    }
  }

  public void clearCofactores() {
    setNombreCofactor(null);
    setCampoB(null);
  }

}
