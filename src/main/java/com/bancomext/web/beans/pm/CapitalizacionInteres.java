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
import java.util.List;

@Data
@ManagedBean(name = "capitalizacionInteres")
@ViewScoped
public class CapitalizacionInteres implements Serializable {

  private static final Logger log = LogManager.getLogger(CapitalizacionInteres.class);
  private final GenericService genericService = ServiceLocator.getGenericService();
  private BigDecimal montoDestino;
  private BigDecimal montoOrigen;
  private BigDecimal montoSust1;
  private BigDecimal sumMontoDestino;
  private BigDecimal sumMontoOrigen;
  private FiCapitalizacionImxTmp beanCapitalizacion;
  private DataTable htmlDataTable; // Para acceder a los elementos de la tabla
  private Integer id;
  private List<FiCapitalizacionImxTmp> listaCapitalizacion;
  private List<FiParametrizacionImx> listaParamCapitalizacion; // Lista para verificar que existan datos en la parametrizacion
  private List<FiProcesoAutManImx> listaProcAutMan; // Lista para verificar que existan datos en la ejecucion de tipo de proceso
  private List<VoSaldosMoneda> listaSaldoMoneda; // Lista que almacena los valores por moneda
  private UsuarioDTO usuarioLogueado;
  private String campoA;
  private String cuentaDestino;
  private String cuentaOrigen;
  private String cuentaSust1;
  private String nomCliente;
  private UIComponent btnActualiza; // boton para mostrar mensaje que se ha actualizado correctamente la tabla
  private UIComponent btnAgregar; // boton para mostrar mensaje que se ha insertado satisfactorimente
  private UIComponent btnElimina; // boton para mostrar que se eliminado correctamente el registro
  private boolean btnUpdateSaveInt; // PARA BOTON DE ALTA MODIFICA
  private boolean paginatorVisible; // PARA PAGINADOR
  private boolean procCanCapInt; // PARA POP UP ALTA Y MODIFICACION DE REGISTROS
  private int defaultRows = Constantes.REGISTROS;

  public CapitalizacionInteres() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("ProcesoManCapitalizacionIntereses", usuarioLogueado.getRol());
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
    listaParamCapitalizacion = genericService.get("FiParametrizacionImx", "OPCODE = 'VF_KL_FUC' AND TIPO_PROCESO = 'CAP'", " order by 1");
    listaProcAutMan = genericService.get("FiProcesoAutManImx", " TIPO_PROCESO = 'CAN' ", "");

    if (listaParamCapitalizacion.isEmpty()) {
      mostrarMensaje("Por favor verifique la Parametrización de la Capitalización. No se encontraron registros", true);
      return;
    }

    if (listaProcAutMan.get(0).getEjecucion() == null) {
      mostrarMensaje("No se encontro ningun tipo de Proceso [Manual o Automático] - Por favor verifique la Ejecución del Proceso", true);
      return;
    }

    if (!listaProcAutMan.get(0).getEjecucion().equals("M")) {
      mostrarMensaje("El proceso se ha detectado como Automático. No puede operar un proceso MANUAL", true);
    }

    try {
      final String error = ProcedimientosService.pCapIntManualCalcula();
      if (error == null || error.isEmpty()) {
        mostrarMensaje("El proceso se ejecutó satisfactoriamente", false);
        sumaMontos();

        listaSaldoMoneda = ConsultasService.getSaldosMoneda();
        paginatorVisible = (listaCapitalizacion.size() > 16);
      } else {
        mostrarMensaje(error, true);
      }
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      mostrarMensaje("El procedimiento no se ejecutó satisfactoriamente", true);
    }
  }

  @SuppressWarnings("unchecked")
  public void sumaMontos() {
    // SE INICIALIZAN VARIALES
    VoSumasMontoSsivac beanSumas = new VoSumasMontoSsivac();
    sumMontoOrigen = new BigDecimal("0.0");
    sumMontoDestino = new BigDecimal("0.0");
    BigDecimal sumMontoSustitucion = new BigDecimal("0.0");

    // CONSULTA PARA LLENAR LA CAPITALIZACION
    listaCapitalizacion = genericService.get("FiCapitalizacionImxTmp", "", " ORDER BY CAMPOA,CUENTA_ORIGEN");

    for (final FiCapitalizacionImxTmp fiCapitalizacionImxTmp : listaCapitalizacion) {

      // OBTIENE VALORES DE LOS MONTOS Y LES APLICA EL FORMATO CON DECIMALES
      if (fiCapitalizacionImxTmp.getMontoOrigen() != null) {
        sumMontoOrigen = sumMontoOrigen.add(fiCapitalizacionImxTmp.getMontoOrigen());
        fiCapitalizacionImxTmp.setMontoOriFor(Utilerias.formatearNumero(fiCapitalizacionImxTmp.getMontoOrigen(), ""));
      }
      if (fiCapitalizacionImxTmp.getMontoDestino() != null) {
        sumMontoDestino = sumMontoDestino.add(fiCapitalizacionImxTmp.getMontoDestino());
        fiCapitalizacionImxTmp.setMontoDesFor(Utilerias.formatearNumero(fiCapitalizacionImxTmp.getMontoDestino(), ""));
      }
      if (fiCapitalizacionImxTmp.getMontoSust1() != null) {
        sumMontoSustitucion = sumMontoSustitucion.add(fiCapitalizacionImxTmp.getMontoSust1());
        fiCapitalizacionImxTmp.setMontoSusFor(Utilerias.formatearNumero(fiCapitalizacionImxTmp.getMontoSust1(), ""));
      }
      if (fiCapitalizacionImxTmp.getMontoSust2() != null) {
        fiCapitalizacionImxTmp.setMontoSust2For(Utilerias.formatearNumero(fiCapitalizacionImxTmp.getMontoSust2(), ""));
      }

      // VALIDA QUE LOS MONTOS NO VENGAN NULOS PARA QUE PUEDA REALIZAR LA COMPARACION DE LOS VALORES
      boolean diferencia;
      if (fiCapitalizacionImxTmp.getMontoOrigen() != null
          && fiCapitalizacionImxTmp.getMontoDestino() != null
          && fiCapitalizacionImxTmp.getMontoSust1() != null) {

        // VALIDACION QUE COMPARA SI LOS MONTOS SON IGUALES PARA COLOCAR IMAGEN DE INCORRECTO O CORRECTO
        diferencia = !fiCapitalizacionImxTmp.getMontoOrigen().equals(fiCapitalizacionImxTmp.getMontoDestino())
            || !fiCapitalizacionImxTmp.getMontoOrigen().equals(fiCapitalizacionImxTmp.getMontoSust1());
        fiCapitalizacionImxTmp.setBandera(diferencia);
      } else {
        fiCapitalizacionImxTmp.setBandera(false);
      }

    }

    // DA FORMATO DECIMALES A LA SUMA DE LOS MONTOS
    beanSumas.setMontoOrigen(Utilerias.formatearNumero(sumMontoOrigen, ""));
    beanSumas.setMontoDestino(Utilerias.formatearNumero(sumMontoDestino, ""));
    beanSumas.setMontoSustitucion1(Utilerias.formatearNumero(sumMontoSustitucion, ""));
  }

  public void eliminarAjuste() {
    FiCapitalizacionImxTmp beanCap;
    try {
      log.info("Eliminar Ajuste");
      int rowIndex = Integer.parseInt(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("rowIndex"));
      beanCap = getListaCapitalizacion().get(rowIndex);
      setBeanCapitalizacion(beanCap);
      genericService.delete(getBeanCapitalizacion());
      sumaMontos();
      mostrarMensaje("Registro eliminado satisfactorimente", false);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
  }

  // SE EJECUTA PROCESO DE ACTUALIZACION (TABLAS EXTEMPORANEAS)
  public void actualizaExtemporaneas() {
    if (sumMontoOrigen.equals(sumMontoDestino)) {
      try {
        log.info("EJECUTA PROCESO ---> PKG_ACCION_CONTABLE_MANUAL_P_CAP_INT_MANUAL_CAMBIOS");
        String error = ProcedimientosService.pCapIntManualCambios();
        if (error == null || error.isEmpty()) {
          mostrarMensaje("El proceso se ejecuto con éxito.", false);
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
    setProcCanCapInt(true);
    setBtnUpdateSaveInt(false);
    int rowIndex;
    try {
      rowIndex = Integer.parseInt(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("rowIndex"));
      FiCapitalizacionImxTmp beanCapitalizacionResp = getListaCapitalizacion().get(rowIndex);
      setId(beanCapitalizacionResp.getId());
      setCampoA(beanCapitalizacionResp.getCampoA());
      setNomCliente(beanCapitalizacionResp.getNomCliente());
      setCuentaOrigen(beanCapitalizacionResp.getCuentaOrigen());
      setMontoOrigen(beanCapitalizacionResp.getMontoOrigen());
      setCuentaDestino(beanCapitalizacionResp.getCuentaDestino());
      setMontoDestino(beanCapitalizacionResp.getMontoDestino());
      setCuentaSust1(beanCapitalizacionResp.getCuentaSust1());
      setMontoSust1(beanCapitalizacionResp.getMontoSust1());
    } catch (Exception ex) {
      setProcCanCapInt(false);
      log.error(ex.getMessage(), ex);
    }
  }

  public void modificaAjuste() {
    log.info("MODIFICA AJUSTE");
    beanCapitalizacion = new FiCapitalizacionImxTmp();
    beanCapitalizacion.setId(getId());
    beanCapitalizacion.setCampoA(getCampoA());
    beanCapitalizacion.setNomCliente(getNomCliente());
    beanCapitalizacion.setCuentaOrigen(getCuentaOrigen());
    beanCapitalizacion.setMontoOrigen(getMontoOrigen());
    beanCapitalizacion.setCuentaDestino(getCuentaDestino());
    beanCapitalizacion.setMontoDestino(getMontoDestino());
    beanCapitalizacion.setCuentaSust1(getCuentaSust1());
    beanCapitalizacion.setMontoSust1(getMontoSust1());
    try {
      genericService.update(beanCapitalizacion);
      clear();
      closePanel();
      sumaMontos();
      mostrarMensaje("Se ha actualizo correctamente el registro", false);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  public void openPanel() {
    procCanCapInt = true;//
    btnUpdateSaveInt = true;
  }

  public void closePanel() {
    procCanCapInt = false;
    clear();
  }

  public void clear() {
    id = 0;
    campoA = null;
    nomCliente = null;
    cuentaOrigen = null;
    montoOrigen = null;
    cuentaDestino = null;
    montoDestino = null;
    cuentaSust1 = null;
  }
}
